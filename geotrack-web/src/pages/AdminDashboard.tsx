import {
  ArrowDownRight,
  ArrowUpRight,
  RefreshCw,
  ShoppingBag,
} from 'lucide-react';
import { useState } from 'react';
import { useGeoTrack } from '../store/GeoTrackContext';

export function AdminDashboard() {
  const { state, createPoi, updatePoiStatus } = useGeoTrack();
  const [poiName, setPoiName] = useState('');
  const [lat, setLat] = useState('24.440716');
  const [lng, setLng] = useState('118.063154');
  const [radius, setRadius] = useState('500');
  const [reward, setReward] = useState('20');
  const [toast, setToast] = useState('');

  return (
    <div className="admin-dash">
      {toast ? (
        <p className="card" style={{ padding: '10px 14px', marginBottom: 12, fontSize: 13 }}>
          {toast}
        </p>
      ) : null}
      <div className="summary-row">
        {[
          { title: '启用 POI', val: '1,248', sub: '较昨日 ↑ 36', up: true, color: '#52c41a' },
          { title: '今日打卡', val: '3,685', sub: '较昨日 ↑ 8.7%', up: true, color: '#1890ff' },
          { title: '上架商品', val: '236', sub: '较昨日 ↑ 12', up: true, color: '#95de64' },
          { title: '秒杀库存', val: '5,432', sub: '较昨日 ↓ 128', up: false, color: '#fa8c16' },
        ].map((c) => (
          <div key={c.title} className="card sum-card">
            <div className="sum-ic" style={{ background: `${c.color}22`, color: c.color }} />
            <div>
              <div className="sum-t">{c.title}</div>
              <div className="sum-v">{c.val}</div>
              <div className={c.up ? 'sum-sub up' : 'sum-sub down'}>
                {c.up ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
                {c.sub}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="split-row">
        <div className="card block">
          <div className="block-head">
            <h3>POI 列表</h3>
            <div className="tools">
              <select className="sel">
                <option>全部状态</option>
              </select>
              <input className="inp" placeholder="搜索 POI 名称" />
              <button type="button" className="icon-only">
                <RefreshCw size={16} />
              </button>
            </div>
          </div>
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>名称</th>
                  <th>坐标</th>
                  <th>半径</th>
                  <th>奖励积分</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {state.pois.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <div className="name-cell">
                        <span className="tb-thumb" />
                        {r.name}
                      </div>
                    </td>
                    <td className="mono">
                      {r.lng}, {r.lat}
                    </td>
                    <td>{r.radius} m</td>
                    <td>{r.rewardPoints}</td>
                    <td>
                      {r.status === 'enabled' ? (
                        <span className="badge ok">启用</span>
                      ) : (
                        <span className="badge bad">禁用</span>
                      )}
                    </td>
                    <td>
                      <button type="button" className="link">
                        编辑
                      </button>{' '}
                      <button
                        type="button"
                        className="link danger"
                        onClick={() => {
                          void (async () => {
                            const res = await updatePoiStatus(r.id, r.status === 'enabled' ? 'disabled' : 'enabled');
                            setToast(res.ok ? res.message : res.message);
                          })();
                        }}
                      >
                        {r.status === 'enabled' ? '禁用' : '启用'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="pager-bar">Total 1,248 条 · 每页 10 条</div>
        </div>

        <div className="card block form-block">
          <h3>创建 / 编辑 POI</h3>
          <label className="fld">
            <span>名称</span>
            <input className="inp full" placeholder="请输入名称" maxLength={50} value={poiName} onChange={(e) => setPoiName(e.target.value)} />
            <small className="cnt">{poiName.length}/50</small>
          </label>
          <div className="fld-row">
            <label className="fld">
              <span>经度</span>
              <input className="inp full" placeholder="118.063154" value={lng} onChange={(e) => setLng(e.target.value)} />
            </label>
            <label className="fld">
              <span>纬度</span>
              <input className="inp full" placeholder="24.440716" value={lat} onChange={(e) => setLat(e.target.value)} />
            </label>
          </div>
          <label className="fld">
            <span>半径（米）</span>
            <input className="inp full" type="number" placeholder="50" value={radius} onChange={(e) => setRadius(e.target.value)} />
            <small className="hint">建议范围：10 - 200 米</small>
          </label>
          <label className="fld">
            <span>奖励积分</span>
            <input className="inp full" type="number" placeholder="50" value={reward} onChange={(e) => setReward(e.target.value)} />
            <small className="hint">用户打卡成功可获得的积分</small>
          </label>
          <label className="fld">
            <span>状态</span>
            <div className="radios">
              <label>
                <input type="radio" name="st" defaultChecked /> 启用
              </label>
              <label>
                <input type="radio" name="st" /> 禁用
              </label>
            </div>
          </label>
          <div className="form-actions">
            <button type="button" className="btn btn-outline">
              重置
            </button>
            <button type="button" className="btn btn-save">
              保存
            </button>
            <button
              type="button"
              className="btn btn-save"
              onClick={() => {
                void (async () => {
                  if (!poiName.trim()) {
                    setToast('请填写 POI 名称');
                    return;
                  }
                  const res = await createPoi({
                    name: poiName.trim(),
                    lat: Number(lat),
                    lng: Number(lng),
                    radius: Number(radius),
                    rewardPoints: Number(reward),
                    status: 'enabled',
                    category: '景点',
                    desc: '管理员创建',
                  });
                  setToast(res.message);
                  if (res.ok) setPoiName('');
                })();
              }}
            >
              新增 POI
            </button>
          </div>
        </div>
      </div>

      <div className="card block">
        <div className="block-head">
          <h3>商品管理（简版）</h3>
          <div className="tools">
            <button type="button" className="btn btn-outline btn-sm">
              查看全部商品
            </button>
            <button type="button" className="btn btn-save btn-sm">
              <ShoppingBag size={14} /> 新增商品
            </button>
          </div>
        </div>
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>商品名称</th>
                <th>类别</th>
                <th>原价</th>
                <th>秒杀价</th>
                <th>库存</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {state.products.map((p) => (
                <tr key={p.id}>
                  <td>
                    <div className="name-cell">
                      <span className="tb-thumb" />
                      {p.name}
                    </div>
                  </td>
                  <td>{p.seckill ? '秒杀商品' : '普通商品'}</td>
                  <td>{Math.round(p.points * 1.8)} 积分</td>
                  <td className="text-warn">{p.points} 积分</td>
                  <td>{p.stock}</td>
                  <td>
                    <span className="badge ok">上架</span>
                  </td>
                  <td>
                    <button type="button" className="link">
                      编辑
                    </button>{' '}
                    <button type="button" className="link danger">
                      下架
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="pager-bar">Total 236 条</div>
      </div>

      <style>{`
        .admin-dash { max-width: 1400px; margin: 0 auto; }
        .summary-row {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 16px;
          margin-bottom: 16px;
        }
        @media (max-width: 1100px) {
          .summary-row { grid-template-columns: repeat(2, 1fr); }
        }
        .sum-card {
          padding: 18px;
          display: flex;
          gap: 14px;
          align-items: flex-start;
        }
        .sum-ic { width: 44px; height: 44px; border-radius: 10px; }
        .sum-t { font-size: 13px; color: var(--text-muted); }
        .sum-v { font-size: 24px; font-weight: 700; margin: 4px 0; }
        .sum-sub { font-size: 12px; display: inline-flex; align-items: center; gap: 2px; }
        .sum-sub.up { color: var(--success); }
        .sum-sub.down { color: var(--danger); }
        .split-row {
          display: grid;
          grid-template-columns: 1.4fr 360px;
          gap: 16px;
          margin-bottom: 16px;
          align-items: start;
        }
        @media (max-width: 1200px) {
          .split-row { grid-template-columns: 1fr; }
        }
        .block { padding: 16px 18px 12px; }
        .block-head {
          display: flex;
          flex-wrap: wrap;
          justify-content: space-between;
          gap: 12px;
          align-items: center;
          margin-bottom: 12px;
        }
        .block-head h3 { margin: 0; font-size: 16px; }
        .tools { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
        .sel, .inp {
          padding: 8px 10px;
          border: 1px solid var(--border);
          border-radius: 6px;
          font-size: 13px;
        }
        .inp { min-width: 140px; }
        .icon-only {
          width: 36px;
          height: 36px;
          border: 1px solid var(--border);
          background: #fff;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
        }
        .table-scroll { overflow-x: auto; }
        .data-table {
          width: 100%;
          border-collapse: collapse;
          font-size: 13px;
        }
        .data-table th {
          text-align: left;
          padding: 10px 8px;
          background: #fafafa;
          border-bottom: 1px solid var(--border);
          color: var(--text-secondary);
          font-weight: 500;
        }
        .data-table td {
          padding: 12px 8px;
          border-bottom: 1px solid var(--border);
          vertical-align: middle;
        }
        .name-cell { display: flex; align-items: center; gap: 10px; }
        .tb-thumb {
          width: 36px;
          height: 36px;
          border-radius: 6px;
          background: #e2e8f0;
          flex-shrink: 0;
        }
        .mono { font-family: ui-monospace, monospace; font-size: 12px; }
        .badge {
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 500;
        }
        .badge.ok { background: #f6ffed; color: var(--success); border: 1px solid #b7eb8f; }
        .badge.bad { background: #fff1f0; color: var(--danger); border: 1px solid #ffa39e; }
        .link {
          background: none;
          border: none;
          color: #1890ff;
          cursor: pointer;
          padding: 0;
          font-size: 13px;
        }
        .link.danger { color: var(--danger); }
        .pager-bar {
          padding: 12px 0 4px;
          font-size: 12px;
          color: var(--text-muted);
        }
        .form-block h3 { margin: 0 0 16px; font-size: 16px; }
        .fld { display: block; margin-bottom: 14px; position: relative; }
        .fld > span { display: block; font-size: 13px; margin-bottom: 6px; color: var(--text-secondary); }
        .fld .full { width: 100%; }
        .fld .cnt {
          position: absolute;
          right: 8px;
          top: 30px;
          font-size: 12px;
          color: var(--text-muted);
        }
        .fld .hint { display: block; margin-top: 6px; font-size: 12px; color: var(--text-muted); }
        .fld-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        .radios { display: flex; gap: 16px; font-size: 14px; }
        .form-actions {
          display: flex;
          gap: 10px;
          margin-top: 20px;
        }
        .btn-save {
          background: var(--admin-green);
          color: #fff;
          border: none;
          padding: 10px 22px;
          border-radius: 6px;
          font-weight: 500;
          cursor: pointer;
        }
        .btn-sm { padding: 8px 14px; font-size: 13px; }
        .text-warn { color: var(--danger); font-weight: 600; }
      `}</style>
    </div>
  );
}
