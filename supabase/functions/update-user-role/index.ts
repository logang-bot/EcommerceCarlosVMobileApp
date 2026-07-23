// update-user-role — SUPERUSUARIO changes another user's role.
// Body: { userId, role }

import { adminClient, handle, json, requireSuperusuario, HttpError } from "../_shared/auth.ts";

Deno.serve(handle(async (req) => {
  await requireSuperusuario(req);

  const { userId, role } = await req.json();
  if (!userId || !role) throw new HttpError(400, "Faltan campos obligatorios");

  const admin = adminClient();

  const { error: authErr } = await admin.auth.admin.updateUserById(userId, {
    user_metadata: { role },
  });
  if (authErr) throw new HttpError(400, authErr.message);

  const { error: dbErr } = await admin.from("users").update({ role }).eq("id", userId);
  if (dbErr) throw new HttpError(400, dbErr.message);

  return json({ ok: true });
}));
