// create-user — SUPERUSUARIO creates a new auth user + `users` profile row.
// Body: { email, password, name, role }
// Returns the created profile row (incl. id) so the client caches it in Room.

import { adminClient, handle, json, requireSuperusuario, HttpError } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  await requireSuperusuario(req);

  const { email, password, name, role } = await req.json();
  if (!email || !password || !name || !role) {
    throw new HttpError(400, "Faltan campos obligatorios");
  }

  const admin = adminClient();

  // 1. Create the Supabase Auth user (auto-confirmed, no verification email).
  const { data: created, error: createErr } = await admin.auth.admin.createUser({
    email: String(email).trim(),
    password,
    email_confirm: true,
    user_metadata: { name: String(name).trim(), role },
  });

  if (createErr || !created.user) {
    const msg = createErr?.message ?? "";
    if (/already been registered|already exists|already registered/i.test(msg)) {
      throw new HttpError(409, "Ya existe un usuario con ese correo");
    }
    throw new HttpError(400, msg || "No se pudo crear el usuario");
  }

  // 2. Insert the profile row (service role — bypasses RLS). Mirror of UserDto.
  const now = Date.now(); // epoch milliseconds
  const row = {
    id: created.user.id,
    email: String(email).trim(),
    name: String(name).trim(),
    role,
    is_active: true,
    created_at: now,
  };

  const { error: insertErr } = await admin.from("users").insert(row);
  if (insertErr) {
    // Roll back the auth user so we don't leave an orphaned account.
    await admin.auth.admin.deleteUser(created.user.id);
    throw new HttpError(400, insertErr.message);
  }

  return json(row, 201);
}));
