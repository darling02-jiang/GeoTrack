import { UserHeader } from '../components/UserHeader';
import {
  CalendarCheck,
  Coins,
  Flame,
  Settings,
} from 'lucide-react';
import { useGeoTrack } from '../store/GeoTrackContext';

export function ProfileOrders() {
  const { state } = useGeoTrack();
  const orders = state.orders.map((item) => ({
    id: item.id,
    name: item.productName,
    sub: item.type === 'seckill' ? '秒杀兑换' : '积分兑换',
    status:
      item.status === 'success' ? '成功' : item.status === 'pending' ? '处理中' : '失败',
    statusType: item.status === 'success' ? 'ok' : item.status === 'pending' ? 'warn' : 'bad',
    time: new Date(item.createdAt).toLocaleString(),
    pts: -item.pointsCost,
  }));
  return (
    <div className="page-shell">
      <UserHeader />

      <div className="page-main prof-layout">
        <aside className="prof-side">
          <div className="card prof-card">
            <div className="prof-cover" />
            <div className="prof-avatar-wrap">
              <span className="prof-avatar" />
            </div>
            <div className="prof-info">
              <div className="prof-name-row">
                <strong>旅行者小明</strong>
                <span className="lv-badge">Lv.5</span>
              </div>
              <p className="prof-bio">
                热爱旅行，探索世界的每一个角落 <Settings size={12} className="inline-ic" />
              </p>
              <div className="prof-stats">
                <div>
                  <Coins size={18} className="c1" />
                  <div className="ps-v">{state.user.points}</div>
                  <div className="ps-l">当前积分</div>
                </div>
                <div>
                  <CalendarCheck size={18} className="c2" />
                  <div className="ps-v">{state.user.checkInCount}</div>
                  <div className="ps-l">累计打卡</div>
                </div>
                <div>
                  <Flame size={18} className="c3" />
                  <div className="ps-v">15</div>
                  <div className="ps-l">连续天数</div>
                </div>
              </div>
            </div>
          </div>
          <div className="card quick-card">
            <h4>快捷入口</h4>
            <div className="quick-grid">
              <a href="/map" className="qk">
                <span className="qk-ic g" />
                我的打卡
                <small>历史记录</small>
              </a>
              <a href="#pts" className="qk">
                <span className="qk-ic b" />
                积分明细
                <small>流水查询</small>
              </a>
              <a href="/feed" className="qk">
                <span className="qk-ic p" />
                我的动态
                <small>内容管理</small>
              </a>
              <a href="#set" className="qk">
                <span className="qk-ic t" />
                资料设置
                <small>账号安全</small>
              </a>
            </div>
          </div>
        </aside>

        <section className="prof-main card">
          <h3 className="om-title">我的订单</h3>
          <div className="om-tabs">
            <button type="button" className="on">
              全部
            </button>
            <button type="button">处理中</button>
            <button type="button">成功</button>
            <button type="button">失败</button>
          </div>
          <div className="table-wrap">
            <table className="om-table">
              <thead>
                <tr>
                  <th>商品信息</th>
                  <th>订单状态</th>
                  <th>下单时间</th>
                  <th>积分消耗</th>
                  <th>订单号</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.id}>
                    <td>
                      <div className="cell-prod">
                        <span className="cell-thumb" />
                        <div>
                          <strong>{o.name}</strong>
                          <div className="sub">{o.sub}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className={`st st-${o.statusType}`}>{o.status}</span>
                    </td>
                    <td>{o.time}</td>
                    <td className={o.pts < 0 ? 'neg' : ''}>{o.pts}</td>
                    <td className="mono">{o.id}</td>
                    <td>
                      <a href={`/orders/${o.id}`} className="btn-mini">
                        查看详情
                      </a>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="om-foot">
            <span>共 {state.orders.length} 条订单</span>
            <div className="pager">
              <button type="button">&lt;</button>
              <button type="button" className="on">
                1
              </button>
              <button type="button">&gt;</button>
              <select>
                <option>10 条/页</option>
              </select>
            </div>
          </div>
        </section>
      </div>

      <footer className="site-footer">
        <div className="site-footer-inner">
          <nav>
            <a href="#a">关于我们</a>
            <a href="#h">帮助中心</a>
            <a href="#u">用户协议</a>
            <a href="#p">隐私政策</a>
          </nav>
          <span>© 2026 GeoTrack 游踪文旅打卡平台. All Rights Reserved.</span>
        </div>
        <style>{`
          .site-footer {
            border-top: 1px solid var(--border);
            background: #fff;
            padding: 20px;
            margin-top: auto;
          }
          .site-footer-inner {
            max-width: 1280px;
            margin: 0 auto;
            display: flex;
            flex-wrap: wrap;
            justify-content: space-between;
            gap: 12px;
            font-size: 13px;
            color: var(--text-muted);
          }
          .site-footer-inner nav {
            display: flex;
            gap: 16px;
          }
          .site-footer-inner a { color: var(--text-secondary); text-decoration: none; }
        `}</style>
      </footer>

      <style>{`
        .prof-layout {
          display: grid;
          grid-template-columns: 300px 1fr;
          gap: 20px;
          align-items: start;
        }
        @media (max-width: 960px) {
          .prof-layout { grid-template-columns: 1fr; }
        }
        .prof-side { display: flex; flex-direction: column; gap: 16px; }
        .prof-card { overflow: hidden; text-align: center; padding-bottom: 20px; }
        .prof-cover {
          height: 100px;
          background: linear-gradient(135deg, #99f6e4, #5eead4);
        }
        .prof-avatar-wrap { margin-top: -40px; display: flex; justify-content: center; }
        .prof-avatar {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          border: 4px solid #fff;
          background: linear-gradient(135deg, #cbd5e1, #94a3b8);
        }
        .prof-info { padding: 12px 16px 0; }
        .prof-name-row {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          margin-bottom: 8px;
        }
        .lv-badge {
          font-size: 11px;
          padding: 2px 8px;
          background: var(--primary-light);
          color: var(--primary-dark);
          border-radius: 999px;
          font-weight: 600;
        }
        .prof-bio {
          font-size: 13px;
          color: var(--text-secondary);
          margin: 0 0 16px;
        }
        .inline-ic { vertical-align: middle; margin-left: 4px; opacity: 0.6; }
        .prof-stats {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 8px;
          padding-top: 16px;
          border-top: 1px solid var(--border);
        }
        .prof-stats > div { font-size: 12px; }
        .ps-v { font-size: 16px; font-weight: 700; margin: 4px 0; }
        .ps-l { color: var(--text-muted); }
        .c1 { color: #d4af37; }
        .c2 { color: #1890ff; }
        .c3 { color: #fa8c16; }
        .quick-card { padding: 16px; }
        .quick-card h4 { margin: 0 0 12px; font-size: 15px; }
        .quick-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 10px;
        }
        .qk {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
          padding: 12px;
          border: 1px solid var(--border);
          border-radius: 10px;
          text-decoration: none;
          color: inherit;
          font-size: 14px;
          font-weight: 600;
        }
        .qk small { font-weight: 400; color: var(--text-muted); margin-top: 4px; }
        .qk-ic {
          width: 28px;
          height: 28px;
          border-radius: 8px;
          margin-bottom: 8px;
        }
        .qk-ic.g { background: #d9f7be; }
        .qk-ic.b { background: #bae7ff; }
        .qk-ic.p { background: #efdbff; }
        .qk-ic.t { background: #b5f5ec; }
        .prof-main { padding: 20px 24px 16px; }
        .om-title { margin: 0 0 16px; font-size: 18px; }
        .om-tabs { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
        .om-tabs button {
          padding: 8px 18px;
          border-radius: 999px;
          border: 1px solid var(--border);
          background: #fff;
          cursor: pointer;
          font-size: 13px;
        }
        .om-tabs button.on {
          background: var(--primary);
          color: #fff;
          border-color: var(--primary);
        }
        .table-wrap { overflow-x: auto; }
        .om-table {
          width: 100%;
          border-collapse: collapse;
          font-size: 13px;
        }
        .om-table th {
          text-align: left;
          padding: 12px 10px;
          background: #fafafa;
          border-bottom: 1px solid var(--border);
          color: var(--text-secondary);
          font-weight: 500;
        }
        .om-table td {
          padding: 14px 10px;
          border-bottom: 1px solid var(--border);
          vertical-align: middle;
        }
        .cell-prod { display: flex; gap: 10px; align-items: center; }
        .cell-thumb {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          background: #e2e8f0;
          flex-shrink: 0;
        }
        .cell-prod .sub { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
        .st-warn { color: #fa8c16; font-weight: 600; }
        .st-ok { color: var(--success); font-weight: 600; }
        .st-bad { color: var(--danger); font-weight: 600; }
        .neg { color: #fa8c16; font-weight: 600; }
        .mono { font-family: ui-monospace, monospace; font-size: 12px; color: var(--text-secondary); }
        .btn-mini {
          display: inline-block;
          padding: 6px 12px;
          border: 1px solid var(--primary);
          color: var(--primary);
          border-radius: 6px;
          font-size: 12px;
          text-decoration: none;
        }
        .om-foot {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 16px;
          font-size: 13px;
          color: var(--text-muted);
        }
        .pager { display: flex; align-items: center; gap: 8px; }
        .pager button {
          min-width: 32px;
          height: 32px;
          border: 1px solid var(--border);
          background: #fff;
          border-radius: 6px;
          cursor: pointer;
        }
        .pager button.on {
          background: var(--primary);
          color: #fff;
          border-color: var(--primary);
        }
        .pager select {
          margin-left: 8px;
          padding: 6px 8px;
          border-radius: 6px;
          border: 1px solid var(--border);
        }
      `}</style>
    </div>
  );
}
