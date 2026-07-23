// reset-user-password — SUPERUSUARIO sets another user's password without
// knowing the current one. Body: { userId, newPassword }

import { adminClient, handle, json, requireSuperusuario, HttpError } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  await requireSuperusuario(req);

  const { userId, newPassword } = await req.json();
  if (!userId || !newPassword) throw new HttpError(400, "Faltan campos obligatorios");

  const admin = adminClient();
  const { error } = await admin.auth.admin.updateUserById(userId, {
    password: newPassword,
  });
  if (error) throw new HttpError(400, error.message);

  return json({ ok: true });
}));
