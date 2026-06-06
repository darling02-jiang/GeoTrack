import { Link } from 'react-router-dom';
import { UserHeader } from '../components/UserHeader';
import {
  Camera,
  Clock,
  Heart,
  MapPin,
  Navigation,
  X,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useGeoTrack } from '../store/GeoTrackContext';

export function PoiDetail() {
  const [text, setText] = useState('');
  const [agree, setAgree] = useState(false);
  const [msg, setMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [userLat, setUserLat] = useState<number | null>(null);
  const [userLng, setUserLng] = useState<number | null>(null);
  const max = 500;
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const { state, checkIn } = useGeoTrack();
  const poi = useMemo(() => state.pois.find((item) => item.id === id), [id, state.pois]);

  useEffect(() => {
    if (!poi) return;
    setUserLat(poi.lat);
    setUserLng(poi.lng);
  }, [poi?.id, poi?.lat, poi?.lng]);

  if (!poi) {
    return (
      <div className="page-shell">
        <UserHeader />
        <div className="page-main">
          <div className="card" style={{ padding: 24 }}>
            <p>未找到该打卡点，或列表尚未从服务端加载。请确认已启动网关与 POI 服务并已登录。</p>
            <Link to="/map" className="btn btn-primary" style={{ marginTop: 12, display: 'inline-block' }}>
              返回地图
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const submit = async () => {
    if (!agree) {
      setMsg('请先勾选社区规范');
      return;
    }
    if (userLat == null || userLng == null) {
      setMsg('请填写或获取定位坐标');
      return;
    }
    setSubmitting(true);
    setMsg('');
    try {
      const result = await checkIn({
        poiId: poi.id,
        lat: userLat,
        lng: userLng,
        text,
        imageUrls: state.uploads.slice(0, 3).map((item) => item.url),
        idempotencyKey: `${poi.id}-${new Date().toDateString()}`,
      });
      setMsg(result.message);
      if (result.ok) navigate('/feed');
    } finally {
      setSubmitting(false);
    }
  };

  const useGps = () => {
    if (!navigator.geolocation) {
      setMsg('当前浏览器不支持定位');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setUserLat(pos.coords.latitude);
        setUserLng(pos.coords.longitude);
        setMsg('已填入当前定位坐标');
      },
      () => setMsg('定位失败，请检查浏览器权限或手动填写坐标'),
      { enableHighAccuracy: true, timeout: 12000 },
    );
  };

  return (
    <div className="page-shell">
      <UserHeader />
      <div className="page-main poi-detail">
        <div className="layout-2">
          <article className="card main-card">
            <div className="hero">
              <span className="hero-badge">景点</span>
              <span className="hero-count">1/8</span>
            </div>
            <div className="poi-body">
              <h1>{poi.name}</h1>
              <div className="tags">
                <span className="pill">5A景区</span>
                <span className="pill">历史文化</span>
                <span className="pill">公园</span>
              </div>
              <p className="addr">
                <MapPin size={16} /> {poi.desc}
              </p>
              <div className="grid-4">
                <div className="info-cell">
                  <Clock size={18} className="ic" />
                  <div>
                    <small>开放时间</small>
                    <div>06:30 - 20:00（全年无休）</div>
                  </div>
                </div>
                <div className="info-cell">
                    <span className="coin">+{poi.rewardPoints} 积分</span>
                  <div>
                    <small>奖励积分</small>
                  </div>
                </div>
                <div className="info-cell">
                  <Navigation size={18} className="ic" />
                  <div>
                    <small>打卡半径</small>
                    <div>{poi.radius} 米</div>
                  </div>
                </div>
                <div className="info-cell" style={{ gridColumn: '1 / -1' }}>
                  <Navigation size={18} className="ic" />
                  <div style={{ flex: 1 }}>
                    <small>打卡坐标（与 POI 距离由服务端校验）</small>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 8, alignItems: 'center' }}>
                      <label style={{ fontSize: 12 }}>
                        纬度
                        <input
                          className="ta"
                          style={{ width: 120, marginLeft: 6, padding: '6px 8px' }}
                          type="number"
                          step="any"
                          value={userLat ?? ''}
                          onChange={(e) => setUserLat(e.target.value === '' ? null : Number(e.target.value))}
                        />
                      </label>
                      <label style={{ fontSize: 12 }}>
                        经度
                        <input
                          className="ta"
                          style={{ width: 120, marginLeft: 6, padding: '6px 8px' }}
                          type="number"
                          step="any"
                          value={userLng ?? ''}
                          onChange={(e) => setUserLng(e.target.value === '' ? null : Number(e.target.value))}
                        />
                      </label>
                      <button type="button" className="btn btn-outline" style={{ fontSize: 12 }} onClick={useGps}>
                        使用当前定位
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <section className="upload-block">
                <label>上传图片（最多 9 张）</label>
                <div className="upload-row">
                  <Link to="/upload" className="upload-dashed">
                    <Camera size={28} />
                  </Link>
                  {state.uploads.slice(0, 4).map((item) => (
                    <div key={item.id} className="thumb">
                      <button type="button" className="thumb-x" aria-label="删除">
                        <X size={12} />
                      </button>
                    </div>
                  ))}
                </div>
              </section>

              <section>
                <label>打卡文案</label>
                <div className="ta-wrap">
                  <textarea
                    className="ta"
                    placeholder="说说你在这里的发现和感受吧..."
                    value={text}
                    maxLength={max}
                    onChange={(e) => setText(e.target.value)}
                    rows={5}
                  />
                  <span className="ta-count">
                    {text.length}/{max}
                  </span>
                </div>
              </section>

              <button type="button" className="btn btn-primary btn-block" disabled={submitting} onClick={() => void submit()}>
                <MapPin size={18} /> {submitting ? '提交中…' : '发布并打卡'}
              </button>
              <label className="agree">
                <input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)} /> 打卡即表示你已阅读并同意《GeoTrack社区规范》
              </label>
              {msg ? <p className="muted">{msg}</p> : null}
            </div>
          </article>

          <aside className="side-stack">
            <div className="card side-card">
              <h4>打卡规则（与后端一致）</h4>
              <ul className="rules">
                <li>POI 需为「启用」状态；每个自然日，同一账号在同一 POI 仅可成功打卡 1 次</li>
                <li>需在景点打卡半径内（默认 500m，另加约 5m 容差）完成经纬度校验</li>
                <li>须填写打卡文案（≤500 字）、上传图片 URL，并通过服务端校验</li>
                <li>客户端约 2 秒内重复提交会被拦截；具体以服务端提示为准</li>
              </ul>
              <p className="warn">违规内容可能被下架；积分以服务端记录为准</p>
            </div>
            <div className="card side-card">
              <h4>打卡日历与记录</h4>
              <p className="muted" style={{ margin: 0, lineHeight: 1.6 }}>
                「打卡地图」中的日历高亮、已打卡地点数均从服务端实时拉取。若你在库中删除了打卡记录，刷新页面或点击地图页「刷新」即可与数据库对齐。
              </p>
            </div>
            <div className="card side-card">
              <h4>积分奖励说明</h4>
              <ul className="pts-list">
                <li>
                  本 POI 配置奖励：<strong>+{poi.rewardPoints}</strong> 积分 / 次成功打卡
                </li>
                <li>成功打卡后由认证服务入账；同一笔打卡记录不会重复发分（幂等）</li>
                <li>当前无「首次打卡」「优质内容」等额外叠加逻辑，以 POI 的奖励积分数为准</li>
              </ul>
            </div>
            <div className="card side-card">
              <div className="side-head">
                <h4>附近热门动态</h4>
                <a href="#more">更多 &gt;</a>
              </div>
              {[1, 2, 3].map((i) => (
                <div key={i} className="mini-feed">
                  <span className="avatar-tiny" />
                  <div className="mf-body">
                    <div className="mf-top">
                      <strong>用户{i}</strong>
                      <span className="muted">2小时前 · 120m</span>
                    </div>
                    <p>风景太美了，推荐清晨来园...</p>
                    <div className="mf-foot">
                      <span className="mf-thumb" />
                      <span className="likes">
                        <Heart size={12} /> 128
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </aside>
        </div>
      </div>
      <style>{`
        .poi-detail .layout-2 {
          display: grid;
          grid-template-columns: 1fr 320px;
          gap: 20px;
          align-items: start;
        }
        @media (max-width: 1024px) {
          .poi-detail .layout-2 { grid-template-columns: 1fr; }
        }
        .main-card { overflow: hidden; }
        .hero {
          height: 220px;
          background: linear-gradient(135deg, #7dd3c0, #5ab9a8);
          position: relative;
        }
        .hero-badge {
          position: absolute;
          top: 12px;
          right: 12px;
          background: rgba(0,0,0,0.45);
          color: #fff;
          padding: 4px 10px;
          border-radius: 6px;
          font-size: 12px;
        }
        .hero-count {
          position: absolute;
          bottom: 12px;
          right: 12px;
          background: rgba(0,0,0,0.45);
          color: #fff;
          padding: 4px 10px;
          border-radius: 6px;
          font-size: 12px;
        }
        .poi-body { padding: 24px; }
        .poi-body h1 { margin: 0 0 12px; font-size: 26px; }
        .tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; }
        .pill {
          padding: 4px 10px;
          background: var(--primary-light);
          color: var(--primary-dark);
          border-radius: 999px;
          font-size: 12px;
        }
        .addr {
          display: flex;
          align-items: center;
          gap: 6px;
          color: var(--text-secondary);
          font-size: 14px;
          margin: 0 0 20px;
        }
        .grid-4 {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 12px;
          margin-bottom: 24px;
        }
        @media (max-width: 600px) {
          .grid-4 { grid-template-columns: 1fr; }
        }
        .info-cell {
          display: flex;
          gap: 10px;
          padding: 12px;
          background: #fafafa;
          border-radius: var(--radius);
          align-items: flex-start;
        }
        .info-cell small { color: var(--text-muted); display: block; margin-bottom: 4px; }
        .info-cell .ic { color: var(--primary); flex-shrink: 0; }
        .coin { font-weight: 700; color: #d4af37; font-size: 16px; }
        .upload-block { margin-bottom: 20px; }
        .upload-block label, .poi-body > section > label {
          display: block;
          font-weight: 500;
          margin-bottom: 8px;
          font-size: 14px;
        }
        .upload-row {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
        }
        .upload-dashed {
          width: 72px;
          height: 72px;
          border: 2px dashed var(--primary);
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--primary);
          text-decoration: none;
        }
        .thumb {
          width: 72px;
          height: 72px;
          border-radius: 10px;
          background: linear-gradient(135deg, #cbd5e1, #94a3b8);
          position: relative;
        }
        .thumb-x {
          position: absolute;
          top: 4px;
          right: 4px;
          width: 20px;
          height: 20px;
          border-radius: 50%;
          border: none;
          background: rgba(0,0,0,0.5);
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
        }
        .ta-wrap { position: relative; }
        .ta {
          width: 100%;
          padding: 12px;
          border: 1px solid var(--border);
          border-radius: var(--radius);
          resize: vertical;
          font-family: inherit;
          font-size: 14px;
          outline: none;
        }
        .ta:focus { border-color: var(--primary); }
        .ta-count {
          position: absolute;
          right: 10px;
          bottom: 10px;
          font-size: 12px;
          color: var(--text-muted);
        }
        .btn-block {
          width: 100%;
          margin-top: 16px;
          padding: 14px;
          font-size: 16px;
        }
        .agree {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-top: 12px;
          font-size: 13px;
          color: var(--text-secondary);
          cursor: pointer;
        }
        .side-stack { display: flex; flex-direction: column; gap: 16px; }
        .side-card { padding: 16px; }
        .side-card h4 { margin: 0 0 12px; font-size: 15px; }
        .rules { margin: 0; padding-left: 18px; font-size: 13px; color: var(--text-secondary); line-height: 1.6; }
        .warn { font-size: 12px; color: var(--warning); margin: 12px 0 0; }
        .muted { font-size: 12px; color: var(--text-muted); margin: 4px 0 0; }
        .pts-list { margin: 0; padding-left: 18px; font-size: 13px; line-height: 1.8; color: var(--text-secondary); }
        .side-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
        .side-head h4 { margin: 0; }
        .mini-feed {
          display: flex;
          gap: 10px;
          padding: 10px 0;
          border-bottom: 1px solid var(--border);
        }
        .mini-feed:last-child { border-bottom: none; }
        .avatar-tiny {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          background: #d9d9d9;
          flex-shrink: 0;
        }
        .mf-body { flex: 1; min-width: 0; }
        .mf-top { display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 4px; }
        .mf-body p { margin: 0; font-size: 13px; color: var(--text-secondary); }
        .mf-foot { display: flex; align-items: center; gap: 10px; margin-top: 6px; }
        .mf-thumb { width: 40px; height: 40px; border-radius: 6px; background: #e2e8f0; }
        .likes { font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; }
      `}</style>
    </div>
  );
}
