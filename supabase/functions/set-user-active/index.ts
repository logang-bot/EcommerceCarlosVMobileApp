// set-user-active — SUPERUSUARIO deactivates/reactivates another user.
// Body: { userId, isActive }
// Supabase Auth has no permanent-ban flag; "876000h" (~100 years) is the
// maximum supported duration and serves as an indefinite disable.

import { adminClient, handle, json, requireSuperusuario, HttpError } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  await requireSuperusuario(req);

  const { userId, isActive } = await req.json();
  if (!userId || typeof isActive !== "boolean") {
    throw new HttpError(400, "Faltan campos obligatorios");
  }

  const admin = adminClient();

  const { error: authErr } = await admin.auth.admin.updateUserById(userId, {
    ban_duration: isActive ? "none" : "876000h",
  });
  if (authErr) throw new HttpError(400, authErr.message);

  const { error: dbErr } = await admin
    .from("users")
    .update({ is_active: isActive })
    .eq("id", userId);
  if (dbErr) throw new HttpError(400, dbErr.message);

  return json({ ok: true });
}));
