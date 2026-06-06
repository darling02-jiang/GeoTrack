import { Activity, Database, HardDrive, Shield } from 'lucide-react';
import { useGeoTrack } from '../store/GeoTrackContext';

const spark = (
  <svg width="64" height="28" viewBox="0 0 64 28" className="spark">
    <path
      d="M2 20 L12 8 L22 18 L32 6 L42 16 L52 10 L62 14"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
    />
  </svg>
);

export function OpsMonitor() {
  const { state } = useGeoTrack();
  const todayCheckins = state.checkIns.filter((item) => new Date(item.createdAt).toDateString() === new Date().toDateString());
  const successCount = todayCheckins.filter((item) => item.result === 'success').length;
  const successRate = todayCheckins.length ? ((successCount / todayCheckins.length) * 100).toFixed(2) : '100.00';

  return (
    <div className="ops-page">
      <div className="ops-title-bar">
        <h2>运维查询与数据监控</h2>
      </div>

      <div className="ops-body">
        <div className="ops-center">
          <div className="kpi-row">
            {[
              { t: '打卡请求量', v: String(todayCheckins.length), c: '#1890ff', d: '实时统计' },
              { t: '成功率', v: `${successRate}%`, c: '#52c41a', d: '实时统计' },
              { t: '限流次数', v: String(Math.max(todayCheckins.length - successCount, 0)), c: '#faad14', d: '实时统计' },
              { t: '秒杀失败数', v: String(state.orders.filter((item) => item.status === 'failed').length), c: '#ff4d4f', d: '实时统计' },
              { t: '消息积压', v: String(Math.max(state.orders.length - 20, 0)), c: '#722ed1', d: '实时统计' },
            ].map((k) => (
              <div key={k.t} className="card kpi">
                <div className="kpi-top">
                  <span className="kpi-ic" style={{ color: k.c, background: `${k.c}18` }}>
                    <Activity size={18} />
                  </span>
                  <span style={{ color: k.c }}>{spark}</span>
                </div>
                <div className="kpi-t">{k.t}</div>
                <div className="kpi-v" style={{ color: k.c }}>
                  {k.v}
                </div>
                <div className="kpi-d">{k.d}</div>
              </div>
            ))}
          </div>

          <div className="card filter-card">
            <div className="filter-grid">
              <label>
                用户ID
                <input placeholder="请输入用户ID" />
              </label>
              <label>
                POI
                <input placeholder="请输入POI名称/ID" />
              </label>
              <label className="wide">
                时间范围
                <input defaultValue="2025-05-20 00:00:00 ~ 2025-05-20 23:59:59" />
              </label>
              <label>
                订单状态
                <select>
                  <option>全部状态</option>
                </select>
              </label>
            </div>
            <div className="filter-actions">
              <button type="button" className="btn-q">
                查询
              </button>
              <button type="button" className="btn-r">
                重置
              </button>
            </div>
          </div>

          <div className="tables-3">
            <div className="card mini-table">
              <div className="mt-head">
                打卡记录 <a href="#m">更多 &gt;</a>
              </div>
              <table>
                <thead>
                  <tr>
                    <th>时间</th>
                    <th>用户</th>
                    <th>POI</th>
                    <th>结果</th>
                  </tr>
                </thead>
                <tbody>
                  {todayCheckins.slice(0, 3).map((item) => (
                    <tr key={item.id}>
                      <td>{new Date(item.createdAt).toLocaleTimeString()}</td>
                      <td>{item.userId}</td>
                      <td>{state.pois.find((poi) => poi.id === item.poiId)?.name || '-'}</td>
                      <td>
                        <span className={item.result === 'success' ? 't-ok' : 't-bad'}>
                          {item.result === 'success' ? '成功' : '失败'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="card mini-table">
              <div className="mt-head">
                积分流水 <a href="#m">更多 &gt;</a>
              </div>
              <table>
                <thead>
                  <tr>
                    <th>时间</th>
                    <th>类型</th>
                    <th>积分</th>
                    <th>余额</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>10:05</td>
                    <td>打卡奖励</td>
                    <td className="t-ok">+10</td>
                    <td>2860</td>
                  </tr>
                  <tr>
                    <td>10:06</td>
                    <td>兑换消耗</td>
                    <td className="t-bad">-2000</td>
                    <td>860</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div className="card mini-table">
              <div className="mt-head">
                订单状态 <a href="#m">更多 &gt;</a>
              </div>
              <table>
                <thead>
                  <tr>
                    <th>单号</th>
                    <th>类型</th>
                    <th>状态</th>
                    <th>金额</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>GT..742</td>
                    <td>门票</td>
                    <td>
                      <span className="t-ok">已支付</span>
                    </td>
                    <td>2000</td>
                  </tr>
                  <tr>
                    <td>GT..801</td>
                    <td>酒店</td>
                    <td>
                      <span className="t-warn">待支付</span>
                    </td>
                    <td>5000</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <aside className="ops-side">
          {[
            {
              title: 'Redis GEO',
              tag: '运行中',
              rows: [
                ['QPS', '12,456'],
                ['命中率', '98.12%'],
                ['内存', '62%'],
              ],
            },
            {
              title: 'RocketMQ',
              tag: '运行中',
              rows: [
                ['生产 TPS', '8,432'],
                ['消费 TPS', '8,215'],
                ['积压', '8,765'],
                ['延迟', '23ms'],
              ],
            },
            {
              title: 'Sentinel',
              tag: '运行中',
              rows: [
                ['限流 QPS', '2,345'],
                ['熔断次数', '12'],
                ['负载', '45%'],
              ],
            },
            {
              title: 'MinIO',
              tag: '运行中',
              rows: [
                ['存储', '58%'],
                ['对象数', '1,234,567'],
                ['可用', '1.25 TB'],
              ],
            },
          ].map((b) => (
            <div key={b.title} className="card svc-card">
              <div className="svc-head">
                <span className="svc-title">
                  {b.title === 'Redis GEO' && <Database size={16} />}
                  {b.title === 'RocketMQ' && <Activity size={16} />}
                  {b.title === 'Sentinel' && <Shield size={16} />}
                  {b.title === 'MinIO' && <HardDrive size={16} />}
                  {b.title}
                </span>
                <span className="svc-tag">{b.tag}</span>
              </div>
              {b.rows.map(([k, v]) => (
                <div key={k} className="svc-row">
                  <span>{k}</span>
                  <strong>{v}</strong>
                </div>
              ))}
              {b.rows.some((r) => r[0] === '内存' || r[0] === '存储' || r[0] === '负载') && (
                <div className="prog">
                  <div className="prog-in" style={{ width: '62%' }} />
                </div>
              )}
            </div>
          ))}
        </aside>
      </div>

      <footer className="ops-foot">
        © 2026 GeoTrack 游踪文旅打卡平台 | 技术栈: Spring Boot · Redis GEO · RocketMQ · Sentinel · MinIO · MySQL
      </footer>

      <style>{`
        .ops-page { max-width: 1600px; margin: 0 auto; }
        .ops-title-bar h2 {
          margin: 0 0 16px;
          font-size: 18px;
          font-weight: 600;
        }
        .ops-body {
          display: grid;
          grid-template-columns: 1fr 300px;
          gap: 16px;
          align-items: start;
        }
        @media (max-width: 1200px) {
          .ops-body { grid-template-columns: 1fr; }
        }
        .kpi-row {
          display: grid;
          grid-template-columns: repeat(5, 1fr);
          gap: 12px;
          margin-bottom: 16px;
        }
        @media (max-width: 1400px) {
          .kpi-row { grid-template-columns: repeat(3, 1fr); }
        }
        @media (max-width: 768px) {
          .kpi-row { grid-template-columns: 1fr; }
        }
        .kpi { padding: 14px; }
        .kpi-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
        .kpi-ic {
          width: 36px;
          height: 36px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .spark { opacity: 0.85; }
        .kpi-t { font-size: 12px; color: var(--text-muted); }
        .kpi-v { font-size: 20px; font-weight: 700; margin: 4px 0; }
        .kpi-d { font-size: 11px; color: var(--text-muted); }
        .filter-card { padding: 16px; margin-bottom: 16px; }
        .filter-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 12px;
          margin-bottom: 12px;
        }
        .filter-grid .wide { grid-column: span 2; }
        @media (max-width: 900px) {
          .filter-grid { grid-template-columns: 1fr; }
          .filter-grid .wide { grid-column: span 1; }
        }
        .filter-grid label {
          display: flex;
          flex-direction: column;
          gap: 6px;
          font-size: 12px;
          color: var(--text-secondary);
        }
        .filter-grid input,
        .filter-grid select {
          padding: 8px 10px;
          border: 1px solid var(--border);
          border-radius: 6px;
          font-size: 13px;
        }
        .filter-actions { display: flex; gap: 10px; }
        .btn-q {
          background: #009688;
          color: #fff;
          border: none;
          padding: 8px 20px;
          border-radius: 6px;
          cursor: pointer;
        }
        .btn-r {
          background: #fff;
          border: 1px solid var(--border);
          padding: 8px 20px;
          border-radius: 6px;
          cursor: pointer;
        }
        .tables-3 {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 12px;
        }
        @media (max-width: 1100px) {
          .tables-3 { grid-template-columns: 1fr; }
        }
        .mini-table { padding: 12px; overflow: auto; }
        .mt-head {
          display: flex;
          justify-content: space-between;
          font-weight: 600;
          margin-bottom: 10px;
          font-size: 14px;
        }
        .mt-head a { font-size: 12px; font-weight: 400; }
        .mini-table table {
          width: 100%;
          border-collapse: collapse;
          font-size: 12px;
        }
        .mini-table th,
        .mini-table td {
          padding: 8px 6px;
          border-bottom: 1px solid var(--border);
          text-align: left;
        }
        .mini-table th { background: #fafafa; color: var(--text-secondary); font-weight: 500; }
        .t-ok { color: var(--success); font-weight: 600; }
        .t-bad { color: var(--danger); font-weight: 600; }
        .t-warn { color: #faad14; font-weight: 600; }
        .ops-side { display: flex; flex-direction: column; gap: 12px; }
        .svc-card { padding: 14px; }
        .svc-head {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
        }
        .svc-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; }
        .svc-tag {
          font-size: 11px;
          padding: 2px 8px;
          background: #f6ffed;
          color: var(--success);
          border-radius: 4px;
          border: 1px solid #b7eb8f;
        }
        .svc-row {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          padding: 4px 0;
          color: var(--text-secondary);
        }
        .svc-row strong { color: var(--text); }
        .prog {
          height: 6px;
          background: #f0f0f0;
          border-radius: 3px;
          margin-top: 8px;
          overflow: hidden;
        }
        .prog-in { height: 100%; background: #009688; border-radius: 3px; }
        .ops-foot {
          text-align: center;
          margin-top: 24px;
          padding: 16px;
          font-size: 12px;
          color: var(--text-muted);
        }
      `}</style>
    </div>
  );
}
