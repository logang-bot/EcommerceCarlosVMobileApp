// delete-user — SUPERUSUARIO deletes another user (auth account + profile row).
// Body: { userId }

import { adminClient, handle, json, requireSuperusuario, HttpError } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  await requireSuperusuario(req);

  const { userId } = await req.json();
  if (!userId) throw new HttpError(400, "Falta el identificador del usuario");

  const admin = adminClient();

  const { error: authErr } = await admin.auth.admin.deleteUser(userId);
  // If the auth user is already gone we still want to clean up the profile row.
  if (authErr && !/not found/i.test(authErr.message)) {
    throw new HttpError(400, authErr.message);
  }

  const { error: dbErr } = await admin.from("users").delete().eq("id", userId);
  if (dbErr) throw new HttpError(400, dbErr.message);

  return json({ ok: true });
}));
