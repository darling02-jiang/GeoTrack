import { Navigate, Route, Routes } from 'react-router-dom';
import { AdminLayout } from './components/AdminLayout';
import { useGeoTrack } from './store/GeoTrackContext';
import { AdminDashboard } from './pages/AdminDashboard';
import { CheckInMap } from './pages/CheckInMap';
import { FeedCircle } from './pages/FeedCircle';
import { Login } from './pages/Login';
import { Mall } from './pages/Mall';
import { OpsMonitor } from './pages/OpsMonitor';
import { OrderDetail } from './pages/OrderDetail';
import { PoiDetail } from './pages/PoiDetail';
import { ProfileOrders } from './pages/ProfileOrders';
import { Upload } from './pages/Upload';

export default function App() {
  const { state } = useGeoTrack();
  const isLogin = Boolean(state.token);

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Navigate to={isLogin ? '/map' : '/login'} replace />} />
      <Route path="/map" element={isLogin ? <CheckInMap /> : <Navigate to="/login" replace />} />
      <Route path="/poi/:id" element={isLogin ? <PoiDetail /> : <Navigate to="/login" replace />} />
      <Route path="/feed" element={isLogin ? <FeedCircle /> : <Navigate to="/login" replace />} />
      <Route path="/mall" element={isLogin ? <Mall /> : <Navigate to="/login" replace />} />
      <Route path="/profile" element={isLogin ? <ProfileOrders /> : <Navigate to="/login" replace />} />
      <Route path="/upload" element={isLogin ? <Upload /> : <Navigate to="/login" replace />} />
      <Route path="/orders/:id" element={isLogin ? <OrderDetail /> : <Navigate to="/login" replace />} />

      <Route path="/admin" element={isLogin ? <AdminLayout /> : <Navigate to="/login" replace />}>
        <Route index element={<AdminDashboard />} />
        <Route path="categories" element={<AdminDashboard />} />
        <Route path="products" element={<AdminDashboard />} />
        <Route path="orders" element={<AdminDashboard />} />
        <Route path="risk" element={<AdminDashboard />} />
        <Route path="ops" element={<OpsMonitor />} />
        <Route path="users" element={<AdminDashboard />} />
        <Route path="settings" element={<AdminDashboard />} />
      </Route>

      <Route path="*" element={<Navigate to={isLogin ? '/map' : '/login'} replace />} />
    </Routes>
  );
}
