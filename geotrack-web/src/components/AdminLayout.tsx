import { Link, Outlet, useLocation } from 'react-router-dom';
import {
  BarChart3,
  Bell,
  ChevronDown,
  ChevronLeft,
  LayoutDashboard,
  MapPin,
  MonitorDot,
  Package,
  Settings,
  Shield,
  ShoppingBag,
  Users,
} from 'lucide-react';
import { useState } from 'react';

const menu = [
  { icon: LayoutDashboard, label: '仪表盘', to: '/admin' },
  {
    icon: MapPin,
    label: 'POI管理',
    children: [
      { label: 'POI列表', to: '/admin' },
      { label: 'POI分类', to: '/admin/categories' },
    ],
  },
  { icon: ShoppingBag, label: '商品管理', to: '/admin/products' },
  { icon: Package, label: '订单管理', to: '/admin/orders' },
  { icon: Shield, label: '风控配置', to: '/admin/risk' },
  { icon: MonitorDot, label: '运维监控', to: '/admin/ops' },
  { icon: Users, label: '用户管理', to: '/admin/users' },
  { icon: Settings, label: '系统设置', to: '/admin/settings' },
];

export function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const { pathname } = useLocation();

  return (
    <div className="admin-root">
      <aside className={`admin-side ${collapsed ? 'collapsed' : ''}`}>
        <div className="admin-brand">
          <MapPin size={22} color="#52c41a" />
          {!collapsed && (
            <div>
              <div className="admin-brand-title">GeoTrack</div>
              <div className="admin-brand-sub">游踪文旅打卡平台</div>
            </div>
          )}
        </div>
        <nav className="admin-menu">
          {menu.map((item) =>
            item.children ? (
              <div key={item.label} className="admin-sub">
                <div className="admin-menu-item parent">
                  <item.icon size={18} />
                  {!collapsed && <span>{item.label}</span>}
                </div>
                {!collapsed &&
                  item.children.map((c) => (
                    <Link
                      key={c.to}
                      to={c.to}
                      className={`admin-menu-item sub ${pathname === c.to ? 'active' : ''}`}
                    >
                      {c.label}
                    </Link>
                  ))}
              </div>
            ) : (
              <Link
                key={item.to}
                to={item.to!}
                className={`admin-menu-item ${pathname === item.to ? 'active' : ''}`}
              >
                <item.icon size={18} />
                {!collapsed && <span>{item.label}</span>}
              </Link>
            )
          )}
        </nav>
        <button type="button" className="collapse-btn" onClick={() => setCollapsed(!collapsed)}>
          <ChevronLeft size={18} style={{ transform: collapsed ? 'rotate(180deg)' : undefined }} />
          {!collapsed && '收起侧边栏'}
        </button>
      </aside>
      <div className="admin-main-wrap">
        <header className="admin-top">
          <div className="admin-top-title">
            <BarChart3 size={20} />
            <span>管理后台</span>
          </div>
          <div className="admin-top-right">
            <button type="button" className="icon-btn-admin">
              <Bell size={20} />
              <span className="badge-dot">12</span>
            </button>
            <button type="button" className="admin-user">
              <span className="avatar-admin" />
              管理员
              <ChevronDown size={16} />
            </button>
          </div>
        </header>
        <main className="admin-content">
          <Outlet />
        </main>
      </div>
      <style>{`
        .admin-root {
          display: flex;
          min-height: 100vh;
          background: #f0f2f5;
        }
        .admin-side {
          width: 220px;
          background: var(--admin-sidebar);
          color: rgba(255,255,255,0.85);
          display: flex;
          flex-direction: column;
          transition: width 0.2s;
        }
        .admin-side.collapsed { width: 72px; }
        .admin-brand {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 20px 16px;
          border-bottom: 1px solid rgba(255,255,255,0.08);
        }
        .admin-brand-title { font-weight: 700; font-size: 16px; }
        .admin-brand-sub { font-size: 11px; opacity: 0.65; }
        .admin-menu { flex: 1; padding: 12px 0; overflow-y: auto; }
        .admin-menu-item {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 12px 20px;
          color: rgba(255,255,255,0.75);
          text-decoration: none;
          font-size: 14px;
        }
        .admin-menu-item:hover { color: #fff; background: rgba(255,255,255,0.05); text-decoration: none; }
        .admin-menu-item.active {
          background: rgba(82, 196, 26, 0.2);
          color: #95de64;
          border-left: 3px solid var(--admin-green);
          padding-left: 17px;
        }
        .admin-menu-item.parent { cursor: default; opacity: 0.9; }
        .admin-menu-item.sub {
          padding-left: 48px;
          font-size: 13px;
        }
        .admin-sub { margin-bottom: 4px; }
        .collapse-btn {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          margin: 12px;
          padding: 10px;
          background: rgba(255,255,255,0.06);
          border: none;
          border-radius: 6px;
          color: rgba(255,255,255,0.7);
          font-size: 13px;
        }
        .admin-main-wrap { flex: 1; display: flex; flex-direction: column; min-width: 0; }
        .admin-top {
          height: 56px;
          background: #fff;
          border-bottom: 1px solid var(--border);
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 0 24px;
        }
        .admin-top-title {
          display: flex;
          align-items: center;
          gap: 10px;
          font-weight: 600;
          color: var(--text);
        }
        .admin-top-right { display: flex; align-items: center; gap: 16px; }
        .icon-btn-admin {
          position: relative;
          background: none;
          border: none;
          padding: 8px;
          color: var(--text-secondary);
        }
        .admin-user {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          background: none;
          border: none;
          font-size: 14px;
        }
        .avatar-admin {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          background: #d9d9d9;
        }
        .admin-content {
          flex: 1;
          padding: 20px 24px 40px;
          overflow: auto;
        }
      `}</style>
    </div>
  );
}
