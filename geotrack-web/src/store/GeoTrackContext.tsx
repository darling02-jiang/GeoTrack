import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { fetchApi } from '../lib/api';

type LoginMode = 'code' | 'password';

export type Poi = {
  id: string;
  name: string;
  lat: number;
  lng: number;
  radius: number;
  rewardPoints: number;
  status: 'enabled' | 'disabled';
  category: string;
  desc: string;
};

export type Post = {
  id: string;
  poiId: string;
  poiName: string;
  userName: string;
  text: string;
  images: string[];
  likes: number;
  comments: { id: string; user: string; text: string; createdAt: string }[];
  createdAt: string;
};

export type FeedHotRankItem = {
  id: string;
  title: string;
  author: string;
  heatLabel: string;
};

type Product = {
  id: string;
  name: string;
  points: number;
  stock: number;
  seckill: boolean;
  perUserLimit: number;
};

type Order = {
  id: string;
  productId: string;
  productName: string;
  pointsCost: number;
  status: 'success' | 'failed' | 'pending';
  type: 'exchange' | 'seckill';
  createdAt: string;
};

type UserProfile = {
  id: string;
  phone: string;
  nickname: string;
  avatar: string;
  bio: string;
  points: number;
  checkInCount: number;
};

type CheckInRecord = {
  id: string;
  poiId: string;
  userId: string;
  result: 'success' | 'failed';
  reason?: string;
  createdAt: string;
  points: number;
};

export type CheckInSummary = {
  year: number;
  month: number;
  checkedDates: string[];
  distinctPoiCount: number;
  totalSuccessCount: number;
};

type ApiCheckInSummary = {
  checkedDates: string[];
  distinctPoiCount: number;
  totalSuccessCount: number;
};

type ApiCheckInRecent = {
  id: number;
  userId: number;
  poiId: number;
  result: string;
  createdAt: string;
};

type UploadItem = {
  id: string;
  name: string;
  md5: string;
  url: string;
  status: 'pending' | 'reviewing' | 'approved';
};

type GeoTrackState = {
  token: string;
  refreshToken: string;
  user: UserProfile;
  pois: Poi[];
  posts: Post[];
  /** 当前圈子 POI（动态列表与热门榜均限定此 POI） */
  feedCirclePoiId: string;
  feedCircleSort: 'latest' | 'likes';
  feedHotRank: FeedHotRankItem[];
  products: Product[];
  orders: Order[];
  checkIns: CheckInRecord[];
  checkInSummary: CheckInSummary | null;
  uploads: UploadItem[];
  riskBlacklistUsers: string[];
  idempotencyMap: Record<string, string>;
  lastActionAtByUser: Record<string, number>;
};

type CheckInPayload = {
  poiId: string;
  lat: number;
  lng: number;
  text: string;
  imageUrls: string[];
  idempotencyKey: string;
};

type GeoTrackContextValue = {
  state: GeoTrackState;
  login: (account: string, secret: string, mode: LoginMode) => Promise<{ ok: boolean; message: string }>;
  sendLoginCode: (phone: string) => Promise<{ ok: boolean; message: string }>;
  logout: () => void;
  refreshLogin: () => void;
  updateProfile: (payload: Partial<Pick<UserProfile, 'nickname' | 'avatar' | 'bio'>>) => void;
  saveUpload: (name: string, md5: string) => { ok: boolean; message: string; url?: string };
  checkIn: (payload: CheckInPayload) => Promise<{ ok: boolean; message: string }>;
  toggleLike: (postId: string) => Promise<void>;
  commentPost: (postId: string, content: string) => Promise<{ ok: boolean; message: string }>;
  setFeedCirclePoi: (poiId: string) => Promise<void>;
  setFeedCircleSort: (sort: 'latest' | 'likes') => Promise<void>;
  refreshFeedCircle: () => Promise<void>;
  createPoi: (payload: Omit<Poi, 'id'>) => Promise<{ ok: boolean; message: string }>;
  updatePoiStatus: (poiId: string, status: Poi['status']) => Promise<{ ok: boolean; message: string }>;
  redeemProduct: (productId: string, type: Order['type']) => Promise<{ ok: boolean; message: string }>;
  refreshCheckInData: (year?: number, month?: number) => Promise<void>;
  refreshMall: () => Promise<void>;
};

const STORAGE_KEY = 'geotrack-demo-store-v1';

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

type ApiPoiRow = {
  id: number;
  name: string;
  longitude: number;
  latitude: number;
  radiusMeters: number;
  rewardPoints: number;
  description: string | null;
  status: number;
};

type ApiUserMe = {
  id: number;
  phone: string;
  nickname: string;
  avatarUrl?: string | null;
  profile?: string | null;
  pointsBalance?: number | null;
};

type ApiCommentView = {
  id: number;
  userId: number;
  content: string;
  createdAt?: string | null;
};

type ApiFeedPoiItem = {
  id: number;
  userId: number;
  poiId: number;
  content: string;
  imageUrl: string;
  likeCount: number;
  commentCount: number;
  hotScore: number;
  createdAt?: string | null;
  commentPreview?: ApiCommentView[];
};

type ApiHotFeedItem = {
  id: number;
  userId: number;
  poiId: number;
  content: string;
  imageUrl: string;
  likeCount: number;
  commentCount: number;
  hotScore: number;
  createdAt?: string | null;
};

type ApiLikeToggleResult = {
  liked: boolean;
  likeCount: number;
};

/** 与后端 MallGoodsDto 对齐 */
type ApiMallGoods = {
  id: number;
  name: string;
  pointsPrice: number;
  stock: number;
  seckill?: boolean;
  isSeckill?: number | boolean;
  beginTime?: string | null;
  endTime?: string | null;
};

/** 与后端 OrderListItemDto 对齐 */
type ApiOrderRow = {
  orderNo: string;
  goodsId: number;
  pointsCost: number;
  status: string;
  createdAt?: string | null;
};

function mapPoiRow(p: ApiPoiRow): Poi {
  return {
    id: String(p.id),
    name: p.name,
    lat: p.latitude,
    lng: p.longitude,
    radius: p.radiusMeters,
    rewardPoints: p.rewardPoints,
    status: p.status === 1 ? 'enabled' : 'disabled',
    category: '景点',
    desc: p.description ?? '',
  };
}

function isSeckillGoods(g: ApiMallGoods): boolean {
  if (g.seckill === true) return true;
  if (g.isSeckill === true || g.isSeckill === 1) return true;
  return false;
}

function mapMallGoodsToProducts(rows: ApiMallGoods[]): Product[] {
  return rows.map((g) => {
    const seckill = isSeckillGoods(g);
    return {
      id: String(g.id),
      name: g.name,
      points: g.pointsPrice,
      stock: g.stock,
      seckill,
      perUserLimit: seckill ? 1 : 99,
    };
  });
}

function mapOrderRows(rows: ApiOrderRow[], products: Product[]): Order[] {
  return rows.map((row) => {
    const pid = String(row.goodsId);
    const p = products.find((x) => x.id === pid);
    const st = row.status === 'PAID' ? ('success' as const) : row.status === 'PENDING' ? ('pending' as const) : ('failed' as const);
    return {
      id: row.orderNo,
      productId: pid,
      productName: p?.name ?? `商品 #${row.goodsId}`,
      pointsCost: row.pointsCost,
      status: st,
      type: p?.seckill ? ('seckill' as const) : ('exchange' as const),
      createdAt: row.createdAt && typeof row.createdAt === 'string' ? row.createdAt : new Date().toISOString(),
    };
  });
}

function mapFeedPoiItems(rows: ApiFeedPoiItem[], pois: Poi[]): Post[] {
  return rows.map((row) => ({
    id: String(row.id),
    poiId: String(row.poiId),
    poiName: pois.find((p) => p.id === String(row.poiId))?.name ?? `POI #${row.poiId}`,
    userName: `用户${row.userId}`,
    text: row.content,
    images: row.imageUrl ? [row.imageUrl] : [],
    likes: row.likeCount ?? 0,
    comments: (row.commentPreview ?? []).map((c) => ({
      id: String(c.id),
      user: `用户${c.userId}`,
      text: c.content,
      createdAt: c.createdAt && typeof c.createdAt === 'string' ? c.createdAt : '',
    })),
    createdAt: row.createdAt && typeof row.createdAt === 'string' ? row.createdAt : new Date().toISOString(),
  }));
}

function formatHeatLabel(h: number): string {
  if (h >= 1_000_000) return `${(h / 1_000_000).toFixed(1)}M`;
  if (h >= 1000) return `${(h / 1000).toFixed(1)}k`;
  return String(Math.round(h));
}

function mapHotFeedItems(rows: ApiHotFeedItem[]): FeedHotRankItem[] {
  return rows.map((row) => ({
    id: String(row.id),
    title: row.content.length > 40 ? `${row.content.slice(0, 40)}…` : row.content,
    author: `用户${row.userId}`,
    heatLabel: formatHeatLabel(row.hotScore ?? 0),
  }));
}

function pickFeedCirclePoiId(prev: GeoTrackState, mapped: Poi[]): string {
  const enabled = mapped.filter((p) => p.status === 'enabled');
  if (prev.feedCirclePoiId && enabled.some((e) => e.id === prev.feedCirclePoiId)) {
    return prev.feedCirclePoiId;
  }
  return enabled[0]?.id ?? '';
}

function initialState(): GeoTrackState {
  return {
    token: '',
    refreshToken: '',
    user: {
      id: '',
      phone: '',
      nickname: '游客',
      avatar: '',
      bio: '',
      points: 0,
      checkInCount: 0,
    },
    pois: [],
    posts: [],
    feedCirclePoiId: '',
    feedCircleSort: 'latest',
    feedHotRank: [],
    products: [],
    orders: [],
    checkIns: [],
    checkInSummary: null,
    uploads: [],
    riskBlacklistUsers: [],
    idempotencyMap: {},
    lastActionAtByUser: {},
  };
}

function persistSnapshot(s: GeoTrackState): string {
  // 打卡、商城商品与订单均以接口为准，不写入 localStorage
  return JSON.stringify({
    ...s,
    checkIns: [],
    checkInSummary: null,
    products: [],
    orders: [],
    posts: [],
    feedHotRank: [],
  });
}

function loadState(): GeoTrackState {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return initialState();
  try {
    const parsed = JSON.parse(raw) as Partial<GeoTrackState>;
    return {
      ...initialState(),
      ...parsed,
      checkIns: [],
      checkInSummary: null,
      products: [],
      orders: [],
      posts: [],
      feedHotRank: [],
    };
  } catch {
    return initialState();
  }
}

async function reloadCheckInDataFromServer(
  commit: (recipe: (prev: GeoTrackState) => GeoTrackState) => void,
  year: number,
  month: number,
) {
  const summaryRes = await fetchApi<ApiCheckInSummary>(
    `/api/checkin/my-summary?year=${year}&month=${month}`,
    { method: 'GET' },
  );
  const recentRes = await fetchApi<ApiCheckInRecent[]>(`/api/checkin/my-recent?limit=50`, { method: 'GET' });

  commit((prev) => {
    let next: GeoTrackState = { ...prev };
    if (summaryRes.ok && summaryRes.data) {
      next = {
        ...next,
        checkInSummary: {
          year,
          month,
          checkedDates: summaryRes.data.checkedDates ?? [],
          distinctPoiCount: summaryRes.data.distinctPoiCount ?? 0,
          totalSuccessCount: summaryRes.data.totalSuccessCount ?? 0,
        },
        user: {
          ...next.user,
          checkInCount: summaryRes.data.totalSuccessCount ?? next.user.checkInCount,
        },
      };
    }
    if (recentRes.ok && Array.isArray(recentRes.data)) {
      next = {
        ...next,
        checkIns: recentRes.data.map((row) => ({
          id: String(row.id),
          poiId: String(row.poiId),
          userId: String(row.userId),
          result: row.result === 'success' ? ('success' as const) : ('failed' as const),
          createdAt: row.createdAt || new Date().toISOString(),
          points: 0,
        })),
      };
    }
    return next;
  });
}

const GeoTrackContext = createContext<GeoTrackContextValue | null>(null);

async function reloadFeedCircleFromServer(
  commit: (recipe: (prev: GeoTrackState) => GeoTrackState) => void,
  pois: Poi[],
  poiId: string,
  sort: 'latest' | 'likes',
) {
  if (!poiId) {
    commit((prev) => ({ ...prev, posts: [], feedHotRank: [] }));
    return;
  }
  const sortParam = sort === 'likes' ? 'likes' : 'latest';
  const [listRes, hotRes] = await Promise.all([
    fetchApi<ApiFeedPoiItem[]>(`/api/feed/poi/${poiId}?sort=${sortParam}&limit=40`, { method: 'GET' }),
    fetchApi<ApiHotFeedItem[]>(`/api/feed/hot?poiId=${poiId}&limit=10`, { method: 'GET' }),
  ]);
  commit((prev) => ({
    ...prev,
    feedCirclePoiId: poiId,
    feedCircleSort: sort,
    posts: listRes.ok && Array.isArray(listRes.data) ? mapFeedPoiItems(listRes.data, pois) : [],
    feedHotRank: hotRes.ok && Array.isArray(hotRes.data) ? mapHotFeedItems(hotRes.data) : [],
  }));
}

async function reloadPoisAndFeeds(commit: (recipe: (prev: GeoTrackState) => GeoTrackState) => void) {
  const poisRes = await fetchApi<ApiPoiRow[]>('/api/poi/list', { method: 'GET' });
  let mapped: Poi[] = [];
  if (poisRes.ok && Array.isArray(poisRes.data)) {
    mapped = poisRes.data.map(mapPoiRow);
  }
  let mergedPois: Poi[] = [];
  let nextPoiId = '';
  let nextSort: 'latest' | 'likes' = 'latest';
  commit((prev) => {
    mergedPois = mapped.length ? mapped : prev.pois;
    nextPoiId = pickFeedCirclePoiId({ ...prev, pois: mergedPois }, mergedPois);
    nextSort = prev.feedCircleSort;
    return { ...prev, pois: mergedPois, feedCirclePoiId: nextPoiId || prev.feedCirclePoiId };
  });
  await reloadFeedCircleFromServer(commit, mergedPois, nextPoiId, nextSort);
}

/** 同一批 commit，避免订单映射时商品列表尚未更新 */
async function reloadMallAndOrders(commit: (recipe: (prev: GeoTrackState) => GeoTrackState) => void) {
  const [goodsRes, orderRes] = await Promise.all([
    fetchApi<ApiMallGoods[]>('/api/mall/goods', { method: 'GET' }),
    fetchApi<ApiOrderRow[]>('/api/order/my?limit=100', { method: 'GET' }),
  ]);
  const products =
    goodsRes.ok && Array.isArray(goodsRes.data) ? mapMallGoodsToProducts(goodsRes.data) : [];
  const orders =
    orderRes.ok && Array.isArray(orderRes.data) ? mapOrderRows(orderRes.data, products) : [];
  commit((prev) => ({ ...prev, products, orders }));
}

export function GeoTrackProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<GeoTrackState>(() => loadState());

  const commit = useCallback((recipe: (prev: GeoTrackState) => GeoTrackState) => {
    setState((prev) => {
      const next = recipe(prev);
      localStorage.setItem(STORAGE_KEY, persistSnapshot(next));
      return next;
    });
  }, []);

  const refreshCheckInData = useCallback(async (year?: number, month?: number) => {
    const d = new Date();
    await reloadCheckInDataFromServer(commit, year ?? d.getFullYear(), month ?? d.getMonth() + 1);
  }, [commit]);

  const refreshMall = useCallback(async () => {
    await reloadMallAndOrders(commit);
  }, [commit]);

  useEffect(() => {
    void (async () => {
      const me = await fetchApi<ApiUserMe>('/api/auth/me', { method: 'GET' });
      if (me.ok && me.data) {
        commit((prev) => ({
          ...prev,
          token: 'session',
          user: {
            ...prev.user,
            id: String(me.data.id),
            phone: me.data.phone,
            nickname: me.data.nickname,
            points: me.data.pointsBalance ?? prev.user.points,
            avatar: me.data.avatarUrl ?? prev.user.avatar,
            bio: me.data.profile ?? prev.user.bio,
          },
        }));
        const d = new Date();
        await reloadCheckInDataFromServer(commit, d.getFullYear(), d.getMonth() + 1);
      }
      await reloadMallAndOrders(commit);
      await reloadPoisAndFeeds(commit);
    })();
  }, [commit]);

  const value = useMemo<GeoTrackContextValue>(
    () => ({
      state,
      sendLoginCode: async (phone) => {
        try {
          const response = await fetch('/api/auth/code/send', {
            method: 'POST',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ phone }),
          });
          const body = (await response.json()) as ApiResponse<string>;
          return { ok: body.code === 0, message: body.code === 0 ? body.data || '发送成功' : body.message };
        } catch {
          return { ok: false, message: '验证码发送失败，请检查网络或服务状态' };
        }
      },
      login: async (account, secret, mode) => {
        if (!account.trim()) return { ok: false, message: '请输入账号或手机号' };
        if (mode !== 'code') return { ok: false, message: '当前仅支持短信验证码登录' };
        if (secret.length < 4) return { ok: false, message: '验证码格式错误' };
        let body: ApiResponse<{ userId: number; nickname: string; phone: string }>;
        try {
          const response = await fetch('/api/auth/login', {
            method: 'POST',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ phone: account, code: secret }),
          });
          body = (await response.json()) as ApiResponse<{ userId: number; nickname: string; phone: string }>;
        } catch {
          return { ok: false, message: '登录失败，请检查网络或服务状态' };
        }
        if (body.code !== 0 || !body.data) {
          return { ok: false, message: body.message || '登录失败' };
        }
        commit((prev) => ({
          ...prev,
          token: 'session',
          refreshToken: '',
          user: {
            ...prev.user,
            id: String(body.data.userId),
            phone: body.data.phone,
            nickname: body.data.nickname,
          },
        }));
        const me = await fetchApi<ApiUserMe>('/api/auth/me', { method: 'GET' });
        if (me.ok && me.data) {
          commit((prev) => ({
            ...prev,
            user: {
              ...prev.user,
              points: me.data.pointsBalance ?? prev.user.points,
              avatar: me.data.avatarUrl ?? prev.user.avatar,
              bio: me.data.profile ?? prev.user.bio,
            },
          }));
        }
        await reloadMallAndOrders(commit);
        await reloadPoisAndFeeds(commit);
        const d = new Date();
        await reloadCheckInDataFromServer(commit, d.getFullYear(), d.getMonth() + 1);
        return { ok: true, message: '登录成功' };
      },
      refreshCheckInData,
      refreshMall,
      logout: () => {
        commit((prev) => ({
          ...prev,
          token: '',
          refreshToken: '',
          checkIns: [],
          checkInSummary: null,
        }));
      },
      refreshLogin: () => {
        commit((prev) => {
          if (!prev.refreshToken) return prev;
          return { ...prev, token: `token-${Date.now()}` };
        });
      },
      updateProfile: (payload) => {
        commit((prev) => ({ ...prev, user: { ...prev.user, ...payload } }));
      },
      saveUpload: (name, md5) => {
        let result: { ok: boolean; message: string; url?: string } = { ok: false, message: '' };
        commit((prev) => {
          const duplicated = prev.uploads.find((item) => item.md5 === md5);
          if (duplicated) {
            result = { ok: false, message: '图片重复提交，已拦截', url: duplicated.url };
            return prev;
          }
          const upload: UploadItem = {
            id: `up-${Date.now()}`,
            name,
            md5,
            url: `https://mock-minio.local/${md5.slice(0, 8)}.jpg`,
            status: 'approved',
          };
          result = { ok: true, message: '上传成功，已生成访问地址', url: upload.url };
          return { ...prev, uploads: [upload, ...prev.uploads] };
        });
        return result;
      },
      checkIn: async (payload) => {
        if (!state.token) return { ok: false, message: '请先登录' };
        const poi = state.pois.find((item) => item.id === payload.poiId);
        if (!poi || poi.status !== 'enabled') return { ok: false, message: 'POI 不可用' };
        if (state.riskBlacklistUsers.includes(state.user.id)) return { ok: false, message: '账号风险受限' };
        const nowTs = Date.now();
        const lastTs = state.lastActionAtByUser[state.user.id] || 0;
        if (nowTs - lastTs < 2000) return { ok: false, message: '请求过于频繁，请稍后再试' };
        if (!payload.text?.trim()) return { ok: false, message: '请填写打卡文案' };
        const imageUrl = payload.imageUrls[0]?.trim();
        if (!imageUrl) return { ok: false, message: '请先上传至少一张图片' };

        let message = '打卡失败';
        try {
          const response = await fetch('/api/checkin', {
            method: 'POST',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
              'X-Idempotency-Key': payload.idempotencyKey,
            },
            body: JSON.stringify({
              poiId: Number(payload.poiId),
              longitude: payload.lng,
              latitude: payload.lat,
              content: payload.text.trim(),
              imageUrl,
            }),
          });
          const body = (await response.json()) as ApiResponse<string>;
          if (body.code !== 0) {
            return { ok: false, message: body.message || message };
          }
          message = typeof body.data === 'string' ? body.data : '打卡成功';
        } catch {
          return { ok: false, message: '打卡请求失败，请检查网关与 POI 服务是否已启动' };
        }

        commit((prev) => ({
          ...prev,
          lastActionAtByUser: { ...prev.lastActionAtByUser, [prev.user.id]: nowTs },
        }));

        const d = new Date();
        await reloadCheckInDataFromServer(commit, d.getFullYear(), d.getMonth() + 1);

        const me = await fetchApi<ApiUserMe>('/api/auth/me', { method: 'GET' });
        if (me.ok && me.data) {
          commit((prev) => ({
            ...prev,
            user: {
              ...prev.user,
              points: me.data.pointsBalance ?? prev.user.points,
            },
          }));
        }
        await reloadPoisAndFeeds(commit);
        return { ok: true, message };
      },
      toggleLike: async (postId) => {
        if (!state.token) return;
        const feedId = Number(postId);
        if (Number.isNaN(feedId)) return;
        const idempotencyKey =
          typeof crypto !== 'undefined' && crypto.randomUUID
            ? crypto.randomUUID()
            : `like-${Date.now()}-${Math.random()}`;
        const res = await fetchApi<ApiLikeToggleResult>('/api/like/toggle', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Idempotency-Key': idempotencyKey,
          },
          body: JSON.stringify({ feedId }),
        });
        if (!res.ok) return;
        await reloadFeedCircleFromServer(
          commit,
          state.pois,
          state.feedCirclePoiId,
          state.feedCircleSort,
        );
      },
      commentPost: async (postId, content) => {
        if (!state.token) return { ok: false, message: '请先登录' };
        if (!content.trim()) return { ok: false, message: '评论不能为空' };
        const feedId = Number(postId);
        if (Number.isNaN(feedId)) return { ok: false, message: '动态无效' };
        const res = await fetchApi<ApiCommentView>('/api/comment', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ feedId, content: content.trim() }),
        });
        if (!res.ok) return { ok: false, message: res.message };
        await reloadFeedCircleFromServer(
          commit,
          state.pois,
          state.feedCirclePoiId,
          state.feedCircleSort,
        );
        return { ok: true, message: '评论成功' };
      },
      setFeedCirclePoi: async (poiId) => {
        commit((prev) => ({ ...prev, feedCirclePoiId: poiId }));
        await reloadFeedCircleFromServer(commit, state.pois, poiId, state.feedCircleSort);
      },
      setFeedCircleSort: async (sort) => {
        commit((prev) => ({ ...prev, feedCircleSort: sort }));
        await reloadFeedCircleFromServer(commit, state.pois, state.feedCirclePoiId, sort);
      },
      refreshFeedCircle: async () => {
        await reloadFeedCircleFromServer(
          commit,
          state.pois,
          state.feedCirclePoiId,
          state.feedCircleSort,
        );
      },
      createPoi: async (payload) => {
        if (!state.token) return { ok: false, message: '请先登录' };
        try {
          const response = await fetch('/api/poi', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              name: payload.name,
              longitude: payload.lng,
              latitude: payload.lat,
              radiusMeters: payload.radius,
              rewardPoints: payload.rewardPoints,
              description: payload.desc || '管理员创建',
              status: payload.status === 'enabled' ? 1 : 0,
            }),
          });
          const body = (await response.json()) as ApiResponse<number>;
          if (body.code !== 0) {
            return { ok: false, message: body.message || '创建失败（需管理员账号）' };
          }
        } catch {
          return { ok: false, message: '创建请求失败，请检查服务与登录状态' };
        }
        await reloadPoisAndFeeds(commit);
        return { ok: true, message: 'POI 已创建' };
      },
      updatePoiStatus: async (poiId, status) => {
        try {
          const response = await fetch(`/api/poi/${poiId}/status`, {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: status === 'enabled' ? 1 : 0 }),
          });
          const body = (await response.json()) as ApiResponse<unknown>;
          if (body.code !== 0) {
            return { ok: false, message: body.message || '更新失败（需管理员）' };
          }
        } catch {
          return { ok: false, message: '更新请求失败' };
        }
        await reloadPoisAndFeeds(commit);
        return { ok: true, message: '状态已更新' };
      },
      redeemProduct: async (productId, type) => {
        if (!state.token) {
          return { ok: false, message: '请先登录' };
        }
        const product = state.products.find((p) => p.id === productId);
        if (!product) {
          return { ok: false, message: '商品不存在' };
        }
        if (product.stock <= 0) {
          return { ok: false, message: '库存不足' };
        }
        const boughtTimes = state.orders.filter(
          (order) => order.productId === productId && order.status === 'success',
        ).length;
        if (type === 'seckill' && boughtTimes >= product.perUserLimit) {
          return { ok: false, message: '已达限购次数' };
        }
        if (state.user.points < product.points) {
          return { ok: false, message: '积分不足' };
        }
        if (type === 'seckill' && !product.seckill) {
          return { ok: false, message: '该商品不是秒杀商品' };
        }
        if (type === 'exchange' && product.seckill) {
          return { ok: false, message: '秒杀商品请使用抢购入口' };
        }
        const goodsId = Number(productId);
        if (Number.isNaN(goodsId)) {
          return { ok: false, message: '商品 ID 无效' };
        }
        const path = type === 'seckill' ? '/api/seckill/order' : '/api/mall/exchange';
        const idempotencyKey =
          typeof crypto !== 'undefined' && crypto.randomUUID
            ? crypto.randomUUID()
            : `idem-${Date.now()}-${Math.random()}`;
        try {
          const response = await fetch(path, {
            method: 'POST',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
              'X-Idempotency-Key': idempotencyKey,
            },
            body: JSON.stringify({ goodsId }),
          });
          const body = (await response.json()) as ApiResponse<{ orderNo: string; status: string }>;
          if (body.code !== 0) {
            return { ok: false, message: body.message || '下单失败' };
          }
        } catch {
          return { ok: false, message: '下单请求失败，请确认网关与商城服务已启动' };
        }
        const me = await fetchApi<ApiUserMe>('/api/auth/me', { method: 'GET' });
        if (me.ok && me.data) {
          commit((prev) => ({
            ...prev,
            user: {
              ...prev.user,
              points: me.data.pointsBalance ?? prev.user.points,
            },
          }));
        }
        await reloadMallAndOrders(commit);
        return { ok: true, message: '下单成功' };
      },
    }),
    [state, commit, refreshCheckInData, refreshMall],
  );

  return <GeoTrackContext.Provider value={value}>{children}</GeoTrackContext.Provider>;
}

export function useGeoTrack() {
  const context = useContext(GeoTrackContext);
  if (!context) throw new Error('useGeoTrack 必须在 GeoTrackProvider 中使用');
  return context;
}
