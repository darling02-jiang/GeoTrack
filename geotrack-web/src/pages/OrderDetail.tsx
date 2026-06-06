import { Link } from 'react-router-dom';
import { UserHeader } from '../components/UserHeader';
import {
  ArrowLeft,
  CheckCircle2,
  FileText,
  Headphones,
  Info,
} from 'lucide-react';
import { useParams } from 'react-router-dom';
import { useGeoTrack } from '../store/GeoTrackContext';

export function OrderDetail() {
  const { id = '' } = useParams();
  const { state } = useGeoTrack();
  const order = state.orders.find((item) => item.id === id) || state.orders[0];
  if (!order) return null;

  return (
    <div className="page-shell">
      <UserHeader variant="mall" username="游小踪" />
      <div className="page-main od-page">
        <nav className="breadcrumb">
          <Link to="/mall">积分商城</Link>
          <span> / </span>
          <span>订单详情</span>
        </nav>

        <div className="od-grid">
          <div className="card od-col">
            <h3>订单状态</h3>
            <ul className="timeline">
              {[
                ['下单成功', '2025-05-20 10:24:15'],
                ['库存扣减', '2025-05-20 10:24:16'],
                ['积分扣减', '2025-05-20 10:24:17'],
                ['订单完成', '2025-05-20 10:24:18'],
              ].map(([t, time]) => (
                <li key={t}>
                  <span className="dot">
                    <CheckCircle2 size={14} />
                  </span>
                  <div>
                    <strong>{t}</strong>
                    <div className="t-muted">{time}</div>
                  </div>
                </li>
              ))}
            </ul>
            <div className="done-card">
              <CheckCircle2 className="text-ok" size={22} />
              <div>
                <strong>订单已完成</strong>
                <p>感谢您的参与，期待下次相遇！</p>
              </div>
            </div>
          </div>

          <div className="card od-col wide">
            <h3>订单详情</h3>
            <div className="prod-row">
              <div className="prod-lg" />
              <div>
                <strong className="prod-name">{order.productName}</strong>
                <span className="tag tag-success" style={{ marginTop: 8, display: 'inline-block' }}>
                  景区门票
                </span>
              </div>
            </div>
            <div className="detail-grid">
              <div>
                <span className="k">订单号</span>
                <div className="v mono">{order.id}</div>
              </div>
              <div>
                <span className="k">兑换类型</span>
                <div className="v">{order.type === 'seckill' ? '秒杀兑换' : '普通兑换'}</div>
              </div>
              <div>
                <span className="k">下单时间</span>
                <div className="v">{new Date(order.createdAt).toLocaleString()}</div>
              </div>
              <div>
                <span className="k">订单状态</span>
                <div
                  className={
                    order.status === 'success' ? 'v text-ok' : order.status === 'pending' ? 'v st-warn' : 'v text-bad'
                  }
                >
                  {order.status === 'success' ? '已完成' : order.status === 'pending' ? '处理中' : '失败'}
                </div>
              </div>
              <div>
                <span className="k">消耗积分</span>
                <div className="v text-warn">{order.pointsCost.toLocaleString()} 积分</div>
              </div>
              <div>
                <span className="k">订单数量</span>
                <div className="v">1 张</div>
              </div>
            </div>
            <div className="verify-box">
              <h4>核销信息</h4>
              <p>
                <span className="text-ok">已核销</span>
              </p>
              <p className="code">8763 4921 0587</p>
              <p>地点：杭州西湖游船码头（东坡路码头）</p>
              <p>时间：2025-05-20 11:35:22</p>
              <p>方式：现场核销（出示核销码）</p>
            </div>
            <div className="info-banner">
              <Info size={18} />
              如需退换或有疑问，请联系在线客服。
            </div>
          </div>

          <div className="od-col side-stack">
            <div className="card panel">
              <h4>积分扣减结果</h4>
              <p className="text-warn big">-{order.pointsCost.toLocaleString()} 积分</p>
              <p>当前余额 {state.user.points.toLocaleString()} 积分</p>
              <span className="tag tag-success">成功</span>
            </div>
            <div className="card panel">
              <h4>库存处理结果</h4>
              <p className="text-warn big">-1</p>
              <p>剩余 28</p>
              <span className="tag tag-success">成功</span>
            </div>
            <div className="card panel ok-panel">
              <CheckCircle2 size={36} className="text-ok" />
              <p>
                <strong>本订单处理正常，无需补偿</strong>
              </p>
              <span className="tag tag-muted">无异常</span>
            </div>
          </div>
        </div>

        <div className="od-actions">
          <Link to="/profile" className="btn btn-outline-primary">
            <ArrowLeft size={16} /> 返回订单列表
          </Link>
          <button type="button" className="btn btn-outline-primary">
            <Headphones size={16} /> 联系客服
          </button>
          <button type="button" className="btn btn-primary" style={{ background: '#1890ff' }}>
            <FileText size={16} /> 查看兑换记录
          </button>
        </div>
      </div>
      <style>{`
        .od-page .breadcrumb {
          font-size: 13px;
          color: var(--text-muted);
          margin-bottom: 16px;
        }
        .od-grid {
          display: grid;
          grid-template-columns: 240px 1fr 260px;
          gap: 16px;
          align-items: start;
        }
        @media (max-width: 1100px) {
          .od-grid { grid-template-columns: 1fr; }
        }
        .od-col { padding: 20px; }
        .od-col.wide { min-width: 0; }
        .od-col h3 { margin: 0 0 16px; font-size: 16px; }
        .timeline { list-style: none; margin: 0; padding: 0; }
        .timeline li {
          display: flex;
          gap: 12px;
          padding-bottom: 16px;
          position: relative;
        }
        .timeline li:not(:last-child)::before {
          content: '';
          position: absolute;
          left: 11px;
          top: 28px;
          bottom: 0;
          width: 2px;
          background: #f0f0f0;
        }
        .dot {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #f6ffed;
          color: var(--success);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          z-index: 1;
        }
        .t-muted { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
        .done-card {
          margin-top: 8px;
          padding: 16px;
          background: #f6ffed;
          border: 1px solid #b7eb8f;
          border-radius: var(--radius);
          display: flex;
          gap: 12px;
          align-items: flex-start;
        }
        .done-card p { margin: 4px 0 0; font-size: 13px; color: var(--text-secondary); }
        .prod-row {
          display: flex;
          gap: 16px;
          margin-bottom: 20px;
        }
        .prod-lg {
          width: 120px;
          height: 80px;
          border-radius: 10px;
          background: linear-gradient(135deg, #7dd3fc, #38bdf8);
          flex-shrink: 0;
        }
        .prod-name { font-size: 16px; }
        .detail-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 14px;
          margin-bottom: 20px;
        }
        @media (max-width: 600px) {
          .detail-grid { grid-template-columns: 1fr; }
        }
        .k { font-size: 12px; color: var(--text-muted); display: block; margin-bottom: 4px; }
        .v { font-size: 14px; }
        .mono { font-family: ui-monospace, monospace; font-size: 13px; }
        .text-ok { color: var(--success); font-weight: 600; }
        .text-warn { color: #fa8c16; font-weight: 600; }
        .text-bad { color: var(--danger); font-weight: 600; }
        .st-warn { color: #fa8c16; font-weight: 600; }
        .verify-box {
          background: #fafafa;
          padding: 16px;
          border-radius: var(--radius);
          margin-bottom: 16px;
          font-size: 13px;
          line-height: 1.6;
        }
        .verify-box h4 { margin: 0 0 10px; }
        .code {
          font-size: 18px;
          font-weight: 700;
          letter-spacing: 2px;
          color: var(--success);
          margin: 8px 0;
        }
        .info-banner {
          display: flex;
          gap: 10px;
          align-items: flex-start;
          padding: 12px 14px;
          background: #e6f7ff;
          border: 1px solid #91d5ff;
          border-radius: 8px;
          font-size: 13px;
          color: var(--text-secondary);
        }
        .side-stack { display: flex; flex-direction: column; gap: 12px; }
        .panel { padding: 16px; }
        .panel h4 { margin: 0 0 10px; font-size: 14px; }
        .panel .big { font-size: 20px; margin: 0 0 8px; }
        .ok-panel { text-align: center; }
        .od-actions {
          display: flex;
          flex-wrap: wrap;
          justify-content: center;
          gap: 12px;
          margin-top: 28px;
        }
        .od-actions .btn {
          text-decoration: none;
        }
      `}</style>
    </div>
  );
}
