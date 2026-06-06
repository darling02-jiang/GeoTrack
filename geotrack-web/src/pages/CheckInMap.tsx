import { Link } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { UserHeader } from '../components/UserHeader';
import { useGeoTrack } from '../store/GeoTrackContext';
import {
  Calendar,
  ChevronLeft,
  ChevronRight,
  LocateFixed,
  MapPin,
  Minus,
  Plus,
  RefreshCw,
  Search,
} from 'lucide-react';

function dayKey(year: number, month: number, day: number) {
  const m = String(month).padStart(2, '0');
  const d = String(day).padStart(2, '0');
  return `${year}-${m}-${d}`;
}

export function CheckInMap() {
  const { state, refreshCheckInData } = useGeoTrack();
  const now = new Date();
  const [calYear, setCalYear] = useState(() => now.getFullYear());
  const [calMonth, setCalMonth] = useState(() => now.getMonth() + 1);

  useEffect(() => {
    if (!state.token) return;
    void refreshCheckInData(calYear, calMonth);
  }, [state.token, calYear, calMonth, refreshCheckInData]);

  const summaryMatches =
    state.checkInSummary &&
    state.checkInSummary.year === calYear &&
    state.checkInSummary.month === calMonth;

  const checkedSet = useMemo(() => {
    if (!summaryMatches || !state.checkInSummary) return new Set<string>();
    return new Set(state.checkInSummary.checkedDates);
  }, [summaryMatches, state.checkInSummary]);

  const calendarCells = useMemo(() => {
    const first = new Date(calYear, calMonth - 1, 1);
    const lastDay = new Date(calYear, calMonth, 0).getDate();
    const startWeekday = first.getDay();
    const cells: { day: number | null }[] = [];
    for (let i = 0; i < startWeekday; i += 1) cells.push({ day: null });
    for (let d = 1; d <= lastDay; d += 1) cells.push({ day: d });
    while (cells.length % 7 !== 0) cells.push({ day: null });
    return cells;
  }, [calYear, calMonth]);

  const distinctPoi = state.checkInSummary?.distinctPoiCount ?? 0;

  const shiftMonth = (delta: number) => {
    const d = new Date(calYear, calMonth - 1 + delta, 1);
    setCalYear(d.getFullYear());
    setCalMonth(d.getMonth() + 1);
  };

  return (
    <div className="page-shell">
      <UserHeader />
      <div className="page-main">
        <div className="map-layout card" style={{ padding: 20, marginBottom: 20 }}>
          <div className="map-col-left">
            <div className="section-head">
              <h3>
                <MapPin size={18} /> 附近打卡
              </h3>
              <button
                type="button"
                className="link-btn"
                onClick={() => {
                  if (state.token) void refreshCheckInData(calYear, calMonth);
                }}
              >
                <RefreshCw size={14} /> 刷新
              </button>
            </div>
            <div className="search-wrap">
              <Search size={16} className="search-ic" />
              <input className="search-inp" placeholder="搜索景点/名称" />
            </div>
            <div className="tag-row">
              {['全部', '景点', '文化', '美食', '公园', '博物馆', '更多'].map((t, i) => (
                <button key={t} type="button" className={i === 0 ? 'tag-chip on' : 'tag-chip'}>
                  {t}
                </button>
              ))}
            </div>
            <ul className="poi-cards">
              {state.pois.map((p) => (
                <li key={p.id} className="poi-card">
                  <div className="poi-thumb" />
                  <div className="poi-meta">
                    <strong>{p.name}</strong>
                    <div className="poi-sub">
                      <MapPin size={12} /> 半径 {p.radius}m · 奖励积分 +{p.rewardPoints}
                    </div>
                  </div>
                  <div className="poi-act">
                    {p.status !== 'enabled' ? (
                      <span className="tag" style={{ background: '#fff1f0', color: '#cf1322', border: '1px solid #ffa39e' }}>
                        已停用
                      </span>
                    ) : (
                      <>
                        <span className="hint-sm">前往打卡</span>
                        <Link to={`/poi/${p.id}`} className="btn btn-primary btn-sm">
                          <LocateFixed size={14} /> 立即打卡
                        </Link>
                      </>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          </div>
          <div className="map-col-right">
            <div className="fake-map">
              <button type="button" className="reloc">
                <LocateFixed size={14} /> 重新定位
              </button>
              <div className="map-circle-label">打卡半径 500m</div>
              <div className="map-user-dot" />
              <div className="map-zoom">
                <button type="button">
                  <Plus size={16} />
                </button>
                <button type="button">
                  <Minus size={16} />
                </button>
                <button type="button">
                  <LocateFixed size={16} />
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="stats-row">
          <div className="stat-card card stat-card-cal">
            <div className="stat-h">
              <Calendar size={18} /> 打卡日历
            </div>
            <div className="cal-nav">
              <button type="button" className="cal-nav-btn" onClick={() => shiftMonth(-1)} aria-label="上月">
                <ChevronLeft size={18} />
              </button>
              <span className="cal-title">
                {calYear} 年 {calMonth} 月
              </span>
              <button type="button" className="cal-nav-btn" onClick={() => shiftMonth(1)} aria-label="下月">
                <ChevronRight size={18} />
              </button>
            </div>
            <div className="cal-weekdays">
              {['日', '一', '二', '三', '四', '五', '六'].map((w) => (
                <span key={w} className="cal-wd">
                  {w}
                </span>
              ))}
            </div>
            <div className="cal-grid">
              {calendarCells.map((cell, idx) => {
                if (cell.day == null) {
                  return <span key={`e-${idx}`} className="cal-cell cal-cell-empty" />;
                }
                const key = dayKey(calYear, calMonth, cell.day);
                const has = checkedSet.has(key);
                const isToday =
                  calYear === now.getFullYear() && calMonth === now.getMonth() + 1 && cell.day === now.getDate();
                return (
                  <span
                    key={key}
                    className={`cal-cell ${has ? 'cal-cell-on' : ''} ${isToday ? 'cal-cell-today' : ''}`}
                    title={has ? '当日有打卡记录' : ''}
                  >
                    {cell.day}
                  </span>
                );
              })}
            </div>
            <p className="cal-legend">
              <span className="cal-dot on" /> 有打卡 &nbsp;
              <span className="cal-dot" /> 无记录
            </p>
            <p className="cal-sync-hint">日历与下方地点数均来自接口，切换月份或点「刷新」会重新拉取。</p>
          </div>
          <div className="stat-card card">
            <div className="stat-h">累计积分</div>
            <div className="stat-big">{state.user.points.toLocaleString()}</div>
            <p className="stat-muted">数据来自认证服务</p>
            <div className="stat-illus trophy" />
          </div>
          <div className="stat-card card">
            <div className="stat-h">
              <MapPin size={18} /> 已打卡地点数
            </div>
            <div className="stat-big">{distinctPoi.toLocaleString()}</div>
            <p className="stat-muted">成功打卡过的不同 POI 数量（与数据库一致）</p>
            <div className="stat-illus pin" />
          </div>
        </div>
      </div>
      <style>{`
        .map-layout {
          display: grid;
          grid-template-columns: minmax(280px, 1fr) 1.6fr;
          gap: 20px;
        }
        @media (max-width: 960px) {
          .map-layout { grid-template-columns: 1fr; }
        }
        .section-head {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 14px;
        }
        .section-head h3 {
          margin: 0;
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 16px;
        }
        .link-btn {
          background: none;
          border: none;
          color: var(--primary);
          font-size: 13px;
          display: inline-flex;
          align-items: center;
          gap: 4px;
          cursor: pointer;
        }
        .search-wrap { position: relative; margin-bottom: 12px; }
        .search-ic {
          position: absolute;
          left: 12px;
          top: 50%;
          transform: translateY(-50%);
          color: var(--text-muted);
        }
        .search-inp {
          width: 100%;
          padding: 10px 12px 10px 36px;
          border: 1px solid var(--border);
          border-radius: 999px;
          outline: none;
        }
        .tag-row {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          margin-bottom: 16px;
        }
        .tag-chip {
          padding: 6px 12px;
          border-radius: 999px;
          border: 1px solid var(--border);
          background: #fff;
          font-size: 12px;
          cursor: pointer;
        }
        .tag-chip.on {
          background: var(--primary);
          color: #fff;
          border-color: var(--primary);
        }
        .poi-cards { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 12px; }
        .poi-card {
          display: grid;
          grid-template-columns: 72px 1fr auto;
          gap: 12px;
          align-items: center;
          padding: 10px;
          border: 1px solid var(--border);
          border-radius: var(--radius);
        }
        .poi-thumb {
          height: 56px;
          border-radius: 8px;
          background: linear-gradient(135deg, #a8e6cf, #88d4c3);
        }
        .poi-meta strong { display: block; font-size: 14px; margin-bottom: 4px; }
        .poi-sub { font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; }
        .poi-act { text-align: right; display: flex; flex-direction: column; gap: 6px; align-items: flex-end; }
        .btn-sm { padding: 6px 12px; font-size: 12px; }
        .hint-sm { font-size: 12px; color: var(--text-muted); }
        .fake-map {
          min-height: 420px;
          border-radius: var(--radius);
          background:
            linear-gradient(180deg, #e8f4f8 0%, #d4e8f0 100%);
          position: relative;
          overflow: hidden;
          border: 1px solid var(--border);
        }
        .reloc {
          position: absolute;
          top: 12px;
          left: 12px;
          z-index: 2;
          padding: 8px 12px;
          border-radius: 8px;
          border: 1px solid var(--border);
          background: #fff;
          font-size: 12px;
          display: inline-flex;
          align-items: center;
          gap: 6px;
        }
        .map-circle-label {
          position: absolute;
          left: 50%;
          top: 50%;
          transform: translate(-50%, -50%);
          width: 280px;
          height: 280px;
          border-radius: 50%;
          border: 2px dashed rgba(38, 182, 167, 0.5);
          background: rgba(38, 182, 167, 0.12);
          display: flex;
          align-items: flex-start;
          justify-content: center;
          padding-top: 24px;
          font-size: 13px;
          color: var(--primary-dark);
          font-weight: 600;
        }
        .map-user-dot {
          position: absolute;
          left: 50%;
          top: 50%;
          width: 14px;
          height: 14px;
          margin: -7px 0 0 -7px;
          border-radius: 50%;
          background: #1890ff;
          box-shadow: 0 0 0 8px rgba(24, 144, 255, 0.25);
        }
        .map-zoom {
          position: absolute;
          right: 12px;
          bottom: 12px;
          display: flex;
          flex-direction: column;
          gap: 4px;
        }
        .map-zoom button {
          width: 36px;
          height: 36px;
          border: 1px solid var(--border);
          background: #fff;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .stats-row {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 16px;
        }
        @media (max-width: 900px) {
          .stats-row { grid-template-columns: 1fr; }
        }
        .stat-card {
          padding: 20px;
          position: relative;
          overflow: hidden;
          min-height: 160px;
        }
        .stat-card-cal { min-height: 280px; }
        .stat-h {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 600;
          margin-bottom: 12px;
          font-size: 14px;
        }
        .cal-nav {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 10px;
        }
        .cal-nav-btn {
          border: 1px solid var(--border);
          background: #fff;
          border-radius: 8px;
          padding: 4px 8px;
          cursor: pointer;
          display: flex;
          align-items: center;
        }
        .cal-title { font-size: 14px; font-weight: 600; }
        .cal-weekdays {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 4px;
          margin-bottom: 6px;
        }
        .cal-wd {
          text-align: center;
          font-size: 11px;
          color: var(--text-muted);
        }
        .cal-grid {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 4px;
        }
        .cal-cell {
          aspect-ratio: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          border-radius: 8px;
          border: 1px solid var(--border);
          background: #fafafa;
        }
        .cal-cell-empty {
          border: none;
          background: transparent;
        }
        .cal-cell-on {
          background: rgba(38, 182, 167, 0.2);
          border-color: var(--primary);
          color: var(--primary-dark);
          font-weight: 600;
        }
        .cal-cell-today:not(.cal-cell-on) {
          outline: 2px solid #1890ff;
          outline-offset: -1px;
        }
        .cal-legend {
          margin: 10px 0 0;
          font-size: 12px;
          color: var(--text-muted);
          display: flex;
          align-items: center;
          gap: 4px;
        }
        .cal-dot {
          display: inline-block;
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: #d9d9d9;
          margin-right: 2px;
        }
        .cal-dot.on { background: var(--primary); }
        .cal-sync-hint {
          margin: 8px 0 0;
          font-size: 11px;
          color: var(--text-muted);
          line-height: 1.4;
        }
        .stat-big { font-size: 28px; font-weight: 700; color: var(--primary); }
        .stat-muted { font-size: 13px; color: var(--text-muted); margin: 4px 0 0; }
        .stat-illus {
          position: absolute;
          right: 16px;
          bottom: 12px;
          width: 64px;
          height: 64px;
          border-radius: 12px;
        }
        .stat-illus.trophy {
          background: linear-gradient(135deg, #ffd666, #fa8c16);
          opacity: 0.85;
        }
        .stat-illus.pin {
          background: linear-gradient(135deg, #bae7ff, #1890ff);
          opacity: 0.75;
        }
      `}</style>
    </div>
  );
}
