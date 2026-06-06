type ApiEnvelope<T> = {
  code: number;
  message: string;
  data: T;
};

export async function fetchApi<T>(
  path: string,
  init?: RequestInit,
): Promise<{ ok: true; data: T } | { ok: false; message: string }> {
  try {
    const res = await fetch(path, {
      credentials: 'include',
      ...init,
    });
    const body = (await res.json()) as ApiEnvelope<T>;
    if (body.code !== 0) {
      return { ok: false, message: body.message || '请求失败' };
    }
    return { ok: true, data: body.data };
  } catch {
    return { ok: false, message: '网络异常或服务不可用' };
  }
}
