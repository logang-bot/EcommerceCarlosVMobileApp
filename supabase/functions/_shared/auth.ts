// Shared helpers for the admin Edge Functions.
//
// Every admin function follows the same contract:
//   1. The caller sends their Supabase Auth JWT in the `Authorization` header
//      (supabase-kt's `functions.invoke` attaches the current session token
//      automatically).
//   2. `requireSuperusuario` verifies that token and confirms the caller is an
//      *active* SUPERUSUARIO by reading their row in the `users` table.
//   3. The function then uses `adminClient()` (service role) to perform the
//      privileged Auth-Admin call plus the matching `users`-table write.
//
// The service-role key is NEVER shipped to the client — Supabase injects
// SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY / SUPABASE_ANON_KEY into every
// function at runtime.

import { createClient, SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2";

export const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

/** JSON response with CORS headers already applied. */
export function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

/** Thrown by `requireSuperusuario`; carries the HTTP status to return. */
export class HttpError extends Error {
  constructor(readonly status: number, message: string) {
    super(message);
  }
}

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY")!;

/** Service-role client — bypasses RLS. Use only inside these functions. */
export function adminClient(): SupabaseClient {
  return createClient(SUPABASE_URL, SERVICE_ROLE_KEY, {
    auth: { autoRefreshToken: false, persistSession: false },
  });
}

/**
 * Verifies the caller's JWT and confirms they are an active SUPERUSUARIO.
 * Returns the caller's auth id. Throws `HttpError` (401/403) otherwise.
 */
export async function requireSuperusuario(req: Request): Promise<string> {
  const authHeader = req.headers.get("Authorization");
  if (!authHeader) throw new HttpError(401, "Falta el token de autorización");

  // A client bound to the caller's token so `getUser()` resolves *their* identity.
  const caller = createClient(SUPABASE_URL, ANON_KEY, {
    global: { headers: { Authorization: authHeader } },
    auth: { autoRefreshToken: false, persistSession: false },
  });

  const { data: userData, error: userErr } = await caller.auth.getUser();
  if (userErr || !userData.user) throw new HttpError(401, "Sesión no válida");
  const callerId = userData.user.id;

  // Look up the caller's role/active flag with the service role (avoids RLS recursion).
  const admin = adminClient();
  const { data: profile, error: profErr } = await admin
    .from("users")
    .select("role, is_active")
    .eq("id", callerId)
    .single();

  if (profErr || !profile) throw new HttpError(403, "Perfil no encontrado");
  if (profile.role !== "SUPERUSUARIO" || profile.is_active !== true) {
    throw new HttpError(403, "Requiere permisos de SUPERUSUARIO");
  }
  return callerId;
}

/** Wraps a handler with CORS preflight + uniform HttpError → JSON handling. */
export function handle(
  fn: (req: Request) => Promise<Response>,
): (req: Request) => Promise<Response> {
  return async (req: Request) => {
    if (req.method === "OPTIONS") {
      return new Response("ok", { headers: corsHeaders });
    }
    try {
      return await fn(req);
    } catch (e) {
      if (e instanceof HttpError) return json({ error: e.message }, e.status);
      console.error(e);
      return json({ error: (e as Error).message ?? "Error interno" }, 500);
    }
  };
}
