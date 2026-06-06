import { Link, useLocation } from 'react-router-dom';
import {
  Bell,
  ChevronDown,
  Coins,
  Home,
  LayoutGrid,
  MapPin,
  Search,
  ShoppingBag,
  User,
  Users,
} from 'lucide-react';
import { useGeoTrack } from '../store/GeoTrackContext';

const navItems = [
  { to: '/map', label: '打卡地图', icon: MapPin },
  { to: '/feed', label: '动态圈子', icon: Users },
  { to: '/mall', label: '积分商城', icon: ShoppingBag },
  { to: '/profile', label: '我的', icon: User },
];

type UserHeaderProps = {
  variant?: 'default' | 'mall' | 'order';
  points?: string;
  username?: string;
};

export function UserHeader({
  variant = 'default',
  points = '',
  username = '',
}: UserHeaderProps) {
  const { pathname } = useLocation();
  const { state, logout } = useGeoTrack();
  const showSearch = variant === 'default' || variant === 'mall';
  const viewPoints = points || state.user.points.toLocaleString();
  const viewUsername = username || state.user.nickname;

  return (
    <header className="user-header">
      <div className="user-header-inner">
        <Link to="/map" className="brand-block">
          <span className="brand-logo" aria-hidden>
            <MapPin size={22} strokeWidth={2.2} />
          </span>
          <span className="brand-text">
            <strong>GeoTrack 游踪</strong>
            <small>文旅打卡平台</small>
          </span>
        </Link>

        <nav className="user-nav">
          <Link
            to="/map"
            className={`nav-item ${pathname === '/map' || pathname === '/' ? 'active' : ''}`}
          >
            <Home size={18} />
            首页
          </Link>
          {navItems.map(({ to, label, icon: Icon }) => (
            <Link key={to} to={to} className={`nav-item ${pathname.startsWith(to) ? 'active' : ''}`}>
              <Icon size={18} />
              {label}
            </Link>
          ))}
        </nav>

        <div className="user-header-right">
          {showSearch && (
            <div className="header-search">
              <Search size={16} className="header-search-icon" />
              <input type="search" placeholder="搜索景点、活动、攻略" className="header-search-input" />
            </div>
          )}
          {variant === 'mall' && (
            <span className="points-pill">
              <Coins size={16} />
              我的积分 {viewPoints}
            </span>
          )}
          {variant !== 'mall' && (
            <span className="points-inline">
              <Coins size={16} className="text-gold" />
              {viewPoints} 积分
              <ChevronDown size={14} />
            </span>
          )}
          <button type="button" className="icon-btn" aria-label="通知">
            <Bell size={20} />
            <span className="badge-dot">3</span>
          </button>
          <button type="button" className="user-chip">
            <span className="avatar-sm" />
            <span>{viewUsername}</span>
            <ChevronDown size={14} />
          </button>
          <button type="button" className="logout-btn" onClick={logout}>
            退出
          </button>
        </div>
      </div>
      <style>{`
        .user-header {
          background: #fff;
          border-bottom: 1px solid var(--border);
          position: sticky;
          top: 0;
          z-index: 50;
        }
        .user-header-inner {
          max-width: 1280px;
          margin: 0 auto;
          padding: 0 20px;
          height: 64px;
          display: flex;
          align-items: center;
          gap: 24px;
        }
        .brand-block {
          display: flex;
          align-items: center;
          gap: 10px;
          color: inherit;
          text-decoration: none;
        }
        .brand-block:hover { text-decoration: none; }
        .brand-logo {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: linear-gradient(135deg, #26b6a7, #1da1f2);
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .brand-text {
          display: flex;
          flex-direction: column;
          line-height: 1.2;
        }
        .brand-text strong { font-size: 16px; color: #0d9488; }
        .brand-text small { font-size: 11px; color: var(--text-muted); }
        .user-nav {
          flex: 1;
          display: flex;
          justify-content: center;
          gap: 8px;
        }
        .nav-item {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 8px 14px;
          border-radius: 8px;
          color: var(--text-secondary);
          font-size: 14px;
          text-decoration: none;
        }
        .nav-item:hover { color: var(--primary); text-decoration: none; }
        .nav-item.active {
          color: var(--primary);
          font-weight: 600;
          box-shadow: inset 0 -2px 0 var(--primary);
        }
        .user-header-right {
          display: flex;
          align-items: center;
          gap: 16px;
        }
        .header-search {
          position: relative;
          width: 220px;
        }
        .header-search-icon {
          position: absolute;
          left: 10px;
          top: 50%;
          transform: translateY(-50%);
          color: var(--text-muted);
        }
        .header-search-input {
          width: 100%;
          padding: 8px 10px 8px 34px;
          border: 1px solid var(--border);
          border-radius: 999px;
          font-size: 13px;
          outline: none;
        }
        .header-search-input:focus {
          border-color: var(--primary);
        }
        .points-inline, .points-pill {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          color: var(--text-secondary);
        }
        .points-pill {
          background: #f6ffed;
          border: 1px solid #b7eb8f;
          color: #389e0d;
          padding: 6px 12px;
          border-radius: 999px;
          font-weight: 500;
        }
        .text-gold { color: #d4af37; }
        .icon-btn {
          position: relative;
          background: none;
          border: none;
          padding: 8px;
          color: var(--text-secondary);
        }
        .user-chip {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          background: none;
          border: none;
          font-size: 14px;
          color: var(--text);
        }
        .avatar-sm {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: linear-gradient(135deg, #94a3b8, #64748b);
        }
        .logout-btn {
          border: 1px solid var(--border);
          border-radius: 6px;
          background: #fff;
          color: var(--text-secondary);
          font-size: 12px;
          padding: 6px 10px;
        }
        @media (max-width: 1024px) {
          .user-nav { display: none; }
          .header-search { display: none; }
        }
      `}</style>
    </header>
  );
}

export function CategoryScroll() {
  const cats = ['全部', '西湖', '外滩', '故宫', '九寨沟'];
  return (
    <div className="cat-scroll card" style={{ padding: '12px 16px', marginBottom: 20 }}>
      <div style={{ display: 'flex', gap: 10, overflowX: 'auto', alignItems: 'center' }}>
        {cats.map((c, i) => (
          <button
            key={c}
            type="button"
            className={i === 1 ? 'cat-pill active' : 'cat-pill'}
            style={{
              flexShrink: 0,
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              padding: '8px 14px',
              borderRadius: 999,
              border: '1px solid var(--border)',
              background: i === 1 ? 'var(--primary)' : '#fff',
              color: i === 1 ? '#fff' : 'var(--text)',
              cursor: 'pointer',
            }}
          >
            {i === 0 ? <LayoutGrid size={16} /> : <span className="cat-dot" />}
            {c}
          </button>
        ))}
      </div>
    </div>
  );
}
