import { UserHeader } from '../components/UserHeader';
import {
  ChevronRight,
  Coins,
  FileText,
  Filter,
  ShoppingCart,
  Zap,
} from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useGeoTrack } from '../store/GeoTrackContext';

const categories = [
  { id: 'all', label: '全部商品', icon: '📦' },
  { id: 'c', label: '文创周边', icon: '🎨' },
  { id: 'd', label: '生活用品', icon: '🏠' },
  { id: 'e', label: '数码配件', icon: '🎧' },
];

export function Mall() {
  const { state, redeemProduct, refreshMall } = useGeoTrack();
  const [t, setT] = useState({ h: 2, m: 15, s: 36 });
  const [msg, setMsg] = useState('');
  const [skMsg, setSkMsg] = useState('');
  const [busy, setBusy] = useState(false);
  const seckillProduct = useMemo(
    () => state.products.find((item) => item.seckill),
    [state.products],
  );
  useEffect(() => {
    if (state.token && state.products.length === 0) {
      void refreshMall();
    }
  }, [state.token, state.products.length, refreshMall]);

  useEffect(() => {
    const id = window.setInterval(() => {
      setT((prev) => {
        let { h, m, s } = prev;
        s -= 1;
        if (s < 0) {
          s = 59;
          m -= 1;
        }
        if (m < 0) {
          m = 59;
          h -= 1;
        }
        if (h < 0) return { h: 0, m: 0, s: 0 };
        return { h, m, s };
      });
    }, 1000);
    return () => clearInterval(id);
  }, []);

  const pad = (n: number) => String(n).padStart(2, '0');

  return (
    <div className="page-shell">
      <UserHeader variant="mall" points={state.user.points.toLocaleString()} />
      <div className="page-main mall-page">
        <div className="mall-banner card">
          <div className="mb-left">
            <Coins size={40} className="gold-ic" />
            <div>
              <div className="mb-label">积分余额</div>
              <div className="mb-val">{state.user.points.toLocaleString()}</div>
            </div>
            <button type="button" className="btn btn-outline">
              <FileText size={16} /> 兑换记录
            </button>
          </div>
          <div className="mb-right">
            <span className="flash-badge">
              <Zap size={14} /> 秒杀活动进行中
            </span>
            <div className="countdown">
              <span className="muted">距离本场结束</span>
              <div className="cd-boxes">
                <span>{pad(t.h)}</span>:<span>{pad(t.m)}</span>:<span>{pad(t.s)}</span>
              </div>
            </div>
            <a href="#all" className="link-more">
              查看全部秒杀 <ChevronRight size={14} />
            </a>
          </div>
        </div>

        <div className="mall-grid">
          <aside className="mall-cats card">
            {categories.map((c, i) => (
              <button key={c.id} type="button" className={i === 0 ? 'cat-line active' : 'cat-line'}>
                <span>{c.icon}</span> {c.label}
              </button>
            ))}
          </aside>
          <section className="mall-center">
            <div className="tabs-bar card">
              {['综合推荐', '积分从低到高', '积分从高到低', '上新时间'].map((tab, i) => (
                <button key={tab} type="button" className={i === 0 ? 'tab on' : 'tab'}>
                  {tab}
                </button>
              ))}
              <button type="button" className="tab filter">
                <Filter size={14} /> 筛选
              </button>
            </div>
            <div className="prod-grid">
              {state.products.filter((item) => !item.seckill).map((p) => (
                <div key={p.id} className="card prod-card">
                  <div className="prod-img" />
                  <div className="prod-body">
                    <div className="prod-title">{p.name}</div>
                    <div className="prod-price">
                      <Coins size={14} className="gold-ic" />
                      <strong>{p.points.toLocaleString()} 积分</strong>
                    </div>
                    <div className="prod-stock">库存 {p.stock} 件</div>
                    <button
                      type="button"
                      className="btn btn-ghost btn-block-sm"
                      disabled={busy}
                      onClick={async () => {
                        setBusy(true);
                        try {
                          const r = await redeemProduct(p.id, 'exchange');
                          setMsg(r.message);
                        } finally {
                          setBusy(false);
                        }
                      }}
                    >
                      兑换
                    </button>
                  </div>
                </div>
              ))}
            </div>
            {msg ? <p className="muted">{msg}</p> : null}
            <div className="pagination">
              <button type="button">&lt;</button>
              {[1, 2, 3, 4, 5].map((n) => (
                <button key={n} type="button" className={n === 1 ? 'pg on' : 'pg'}>
                  {n}
                </button>
              ))}
              <button type="button">&gt;</button>
            </div>
          </section>
          <aside className="mall-seckill card">
            <div className="sk-head">
              <Zap className="text-orange" size={20} />
              <strong>限量秒杀专区</strong>
              <span className="sk-tag">限时抢购</span>
            </div>
            <div className="sk-img-wrap">
              <span className="hot-badge">热门秒杀</span>
              <div className="sk-photo" />
            </div>
            <h3 className="sk-title">{seckillProduct?.name || '秒杀商品'}</h3>
            <div className="sk-price-row">
              <span className="sk-now">{(seckillProduct?.points || 0).toLocaleString()} 积分</span>
              <span className="sk-old">5,600 积分</span>
            </div>
            <div className="sk-pill">限量 {seckillProduct?.stock || 0} 件</div>
            <p className="sk-left">本场仅剩 {seckillProduct?.stock || 0} 件</p>
            <div className="sk-bar">
              <div className="sk-bar-in" style={{ width: '64%' }} />
            </div>
            <div className="sk-note">
              <ShoppingCart size={16} /> 每人限购 1 件，先到先得
            </div>
            <button
              type="button"
              className="btn-sk"
              disabled={busy || !seckillProduct}
              onClick={async () => {
                setSkMsg('');
                if (!state.token) {
                  setSkMsg('请先登录');
                  return;
                }
                if (!seckillProduct) {
                  setSkMsg('暂无秒杀商品，请确认网关与商城服务已启动');
                  return;
                }
                setBusy(true);
                try {
                  const r = await redeemProduct(seckillProduct.id, 'seckill');
                  setSkMsg(r.message);
                  if (r.ok) setMsg(r.message);
                } finally {
                  setBusy(false);
                }
              }}
            >
              <Zap size={16} /> 立即抢购
            </button>
            {skMsg ? (
              <p className={`sk-msg ${skMsg.includes('成功') ? 'ok' : 'err'}`}>{skMsg}</p>
            ) : null}
            <p className="sk-disclaimer">秒杀商品非质量问题不支持退换</p>
          </aside>
        </div>
      </div>
      <style>{`
        .mall-page .mall-banner {
          display: flex;
          flex-wrap: wrap;
          justify-content: space-between;
          align-items: center;
          padding: 20px 24px;
          margin-bottom: 20px;
          background: linear-gradient(90deg, #e6f4ff, #f0f9ff);
          gap: 16px;
        }
        .mb-left {
          display: flex;
          align-items: center;
          gap: 16px;
        }
        .gold-ic { color: #d4af37; }
        .mb-label { font-size: 13px; color: var(--text-muted); }
        .mb-val { font-size: 28px; font-weight: 700; }
        .mb-right {
          display: flex;
          align-items: center;
          gap: 20px;
          flex-wrap: wrap;
        }
        .flash-badge {
          background: #fff7e6;
          border: 1px solid #ffd591;
          color: #d46b08;
          padding: 6px 12px;
          border-radius: 999px;
          font-size: 13px;
          font-weight: 600;
          display: inline-flex;
          align-items: center;
          gap: 6px;
        }
        .countdown { display: flex; flex-direction: column; gap: 6px; }
        .muted { font-size: 12px; color: var(--text-muted); }
        .cd-boxes {
          font-family: ui-monospace, monospace;
          font-weight: 700;
          color: #d46b08;
          font-size: 18px;
        }
        .cd-boxes span {
          display: inline-block;
          min-width: 28px;
          padding: 4px 8px;
          background: #fff7e6;
          border-radius: 6px;
          margin: 0 2px;
          text-align: center;
        }
        .link-more {
          font-size: 13px;
          display: inline-flex;
          align-items: center;
          gap: 2px;
        }
        .mall-grid {
          display: grid;
          grid-template-columns: 180px 1fr 300px;
          gap: 16px;
          align-items: start;
        }
        @media (max-width: 1100px) {
          .mall-grid { grid-template-columns: 1fr; }
          .mall-cats { display: flex; flex-wrap: wrap; gap: 8px; }
        }
        .mall-cats { padding: 12px; }
        .cat-line {
          display: flex;
          align-items: center;
          gap: 8px;
          width: 100%;
          padding: 12px 14px;
          border: none;
          background: transparent;
          border-radius: 8px;
          text-align: left;
          font-size: 14px;
          cursor: pointer;
          color: var(--text-secondary);
        }
        .cat-line.active {
          background: #e6f4ff;
          color: #1890ff;
          font-weight: 600;
        }
        .tabs-bar {
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          gap: 8px;
          padding: 10px 12px;
          margin-bottom: 16px;
        }
        .tab {
          padding: 8px 12px;
          border: none;
          background: transparent;
          border-radius: 8px;
          font-size: 13px;
          cursor: pointer;
          color: var(--text-secondary);
        }
        .tab.on {
          background: var(--primary);
          color: #fff;
        }
        .tab.filter {
          margin-left: auto;
          display: inline-flex;
          align-items: center;
          gap: 6px;
          border: 1px solid var(--border);
          background: #fff;
        }
        .prod-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 16px;
        }
        @media (max-width: 900px) {
          .prod-grid { grid-template-columns: repeat(2, 1fr); }
        }
        @media (max-width: 520px) {
          .prod-grid { grid-template-columns: 1fr; }
        }
        .prod-card { overflow: hidden; }
        .prod-img {
          height: 140px;
          background: linear-gradient(180deg, #f1f5f9, #e2e8f0);
        }
        .prod-body { padding: 14px; }
        .prod-title { font-weight: 600; margin-bottom: 10px; font-size: 14px; }
        .prod-price {
          display: flex;
          align-items: center;
          gap: 6px;
          color: var(--primary);
          margin-bottom: 6px;
        }
        .prod-stock { font-size: 12px; color: var(--text-muted); margin-bottom: 10px; }
        .btn-block-sm {
          width: 100%;
          padding: 8px;
          font-size: 13px;
        }
        .pagination {
          display: flex;
          justify-content: center;
          gap: 8px;
          margin-top: 24px;
        }
        .pagination button {
          min-width: 36px;
          height: 36px;
          border: 1px solid var(--border);
          background: #fff;
          border-radius: 6px;
          cursor: pointer;
        }
        .pagination .pg.on {
          background: var(--primary);
          color: #fff;
          border-color: var(--primary);
        }
        .mall-seckill {
          padding: 16px;
          background: linear-gradient(180deg, #fff7e6, #fff);
        }
        .sk-head {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;
        }
        .text-orange { color: #fa8c16; }
        .sk-tag {
          margin-left: auto;
          font-size: 11px;
          padding: 2px 8px;
          background: #fff1b8;
          border-radius: 4px;
          color: #ad6800;
        }
        .sk-img-wrap { position: relative; margin-bottom: 12px; }
        .hot-badge {
          position: absolute;
          top: 8px;
          left: 8px;
          z-index: 1;
          background: rgba(0,0,0,0.55);
          color: #fff;
          font-size: 11px;
          padding: 4px 8px;
          border-radius: 6px;
        }
        .sk-photo {
          height: 160px;
          border-radius: 12px;
          background: linear-gradient(135deg, #fed7aa, #fdba74);
        }
        .sk-title { margin: 0 0 8px; font-size: 16px; }
        .sk-price-row { display: flex; align-items: baseline; gap: 10px; margin-bottom: 8px; }
        .sk-now { font-size: 22px; font-weight: 800; color: #d46b08; }
        .sk-old { font-size: 13px; color: var(--text-muted); text-decoration: line-through; }
        .sk-pill {
          display: inline-block;
          padding: 4px 10px;
          background: #ffe7ba;
          border-radius: 999px;
          font-size: 12px;
          color: #ad6800;
          margin-bottom: 8px;
        }
        .sk-left { margin: 0 0 8px; font-size: 13px; color: var(--text-secondary); }
        .sk-bar {
          height: 8px;
          background: #ffe7ba;
          border-radius: 4px;
          overflow: hidden;
          margin-bottom: 12px;
        }
        .sk-bar-in {
          height: 100%;
          background: linear-gradient(90deg, #fa8c16, #ffc069);
          border-radius: 4px;
        }
        .sk-note {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 10px;
          background: #fff7e6;
          border-radius: 8px;
          font-size: 12px;
          color: #ad6800;
          margin-bottom: 12px;
        }
        .btn-sk {
          width: 100%;
          padding: 12px;
          border: none;
          border-radius: 10px;
          background: linear-gradient(90deg, #fa8c16, #ffc069);
          color: #fff;
          font-weight: 700;
          font-size: 15px;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          cursor: pointer;
        }
        .sk-msg {
          margin: 10px 0 0;
          padding: 8px 10px;
          border-radius: 8px;
          font-size: 13px;
          text-align: center;
        }
        .sk-msg.ok { background: #f6ffed; color: #389e0d; }
        .sk-msg.err { background: #fff2f0; color: #cf1322; }
        .sk-disclaimer { font-size: 11px; color: var(--text-muted); text-align: center; margin: 10px 0 0; }
      `}</style>
    </div>
  );
}
