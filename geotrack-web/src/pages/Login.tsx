import { Gift, MapPin, MessageCircle, Smartphone, Shield } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useGeoTrack } from '../store/GeoTrackContext';

export function Login() {
  const [account, setAccount] = useState('13800000000');
  const [secret, setSecret] = useState('');
  const [msg, setMsg] = useState('');
  const [countdown, setCountdown] = useState(0);
  const { login, sendLoginCode } = useGeoTrack();
  const navigate = useNavigate();

  useEffect(() => {
    if (countdown <= 0) return;
    const timer = window.setTimeout(() => setCountdown((prev) => prev - 1), 1000);
    return () => window.clearTimeout(timer);
  }, [countdown]);

  const submit = async () => {
    const result = await login(account, secret, 'code');
    setMsg(result.message);
    if (result.ok) navigate('/map');
  };

  const onSendCode = async () => {
    if (countdown > 0) return;
    const result = await sendLoginCode(account);
    setMsg(result.message);
    if (result.ok) {
      setCountdown(60);
    }
  };

  return (
    <div className="login-page">
      <div className="login-left">
        <div className="login-brand-row">
          <span className="login-logo">
            <MapPin size={28} color="#fff" />
          </span>
          <h1 className="login-title-gradient">GeoTrack 游踪</h1>
        </div>
        <p className="login-slogan">到此一游，积分到手</p>
        <div className="login-divider" />
        <div className="login-features">
          <div className="feat">
            <span className="feat-icon teal">
              <MapPin size={18} color="#fff" />
            </span>
            <span>LBS打卡</span>
          </div>
          <div className="feat-sep" />
          <div className="feat">
            <span className="feat-icon green">
              <Gift size={18} color="#fff" />
            </span>
            <span>积分激励</span>
          </div>
          <div className="feat-sep" />
          <div className="feat">
            <span className="feat-icon blue">
              <MessageCircle size={18} color="#fff" />
            </span>
            <span>文旅圈子</span>
          </div>
        </div>
      </div>
      <div className="login-right">
        <div className="login-card card">
          <h2>欢迎登录 GeoTrack 游踪</h2>
          <p className="login-sub">探索更多精彩，记录你的足迹</p>
          <div className="login-field">
            <Smartphone className="field-icon" size={18} />
            <input
              className="login-input"
              placeholder="请输入手机号"
              value={account}
              onChange={(e) => setAccount(e.target.value)}
            />
          </div>
          <div className="login-row-code">
            <div className="login-field flex1">
              <Shield className="field-icon" size={18} />
              <input
                className="login-input no-pad"
                placeholder="请输入验证码"
                value={secret}
                onChange={(e) => setSecret(e.target.value)}
              />
            </div>
            <button type="button" className="btn-code" onClick={onSendCode} disabled={countdown > 0}>
              {countdown > 0 ? `${countdown}s后重试` : '获取验证码'}
            </button>
          </div>
          <button type="button" className="btn btn-primary login-submit" onClick={submit}>
            登录
          </button>
          {msg ? <p className="login-msg">{msg}</p> : null}
          <div className="login-or">
            <span>其他登录方式</span>
          </div>
          <div className="wechat-wrap">
            <span className="wechat-btn" title="微信登录" />
          </div>
          <p className="login-alt">短信验证码登录</p>
          <div className="login-footer">
            还没有账号？<a href="#reg">立即注册</a>
          </div>
        </div>
      </div>
      <style>{`
        .login-page {
          min-height: 100vh;
          display: flex;
          background: linear-gradient(180deg, #f8fffe 0%, #e6f7f5 45%, #d0f0ed 100%);
          position: relative;
          overflow: hidden;
        }
        .login-page::before {
          content: '';
          position: absolute;
          inset: 0;
          background:
            radial-gradient(ellipse 80% 50% at 20% 80%, rgba(38, 182, 167, 0.25), transparent),
            radial-gradient(ellipse 60% 40% at 70% 90%, rgba(29, 161, 242, 0.2), transparent);
          pointer-events: none;
        }
        .login-left {
          flex: 1;
          padding: 48px 56px;
          position: relative;
          z-index: 1;
          display: flex;
          flex-direction: column;
          justify-content: center;
          max-width: 520px;
        }
        .login-brand-row {
          display: flex;
          align-items: center;
          gap: 14px;
          margin-bottom: 12px;
        }
        .login-logo {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          background: linear-gradient(135deg, #26b6a7, #1da1f2);
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .login-title-gradient {
          margin: 0;
          font-size: 28px;
          background: linear-gradient(90deg, #26b6a7, #1da1f2);
          -webkit-background-clip: text;
          background-clip: text;
          color: transparent;
        }
        .login-slogan {
          color: var(--text-secondary);
          font-size: 16px;
          margin: 0 0 20px;
        }
        .login-divider {
          height: 2px;
          max-width: 200px;
          background: linear-gradient(90deg, #26b6a7, #1da1f2);
          border-radius: 2px;
          margin-bottom: 28px;
        }
        .login-features {
          display: flex;
          align-items: center;
          gap: 16px;
        }
        .feat {
          display: flex;
          align-items: center;
          gap: 10px;
          font-size: 14px;
          color: var(--text-secondary);
        }
        .feat-icon {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .feat-icon.teal { background: #26b6a7; }
        .feat-icon.green { background: #52c41a; }
        .feat-icon.blue { background: #1da1f2; }
        .feat-sep { width: 1px; height: 28px; background: #ddd; }
        .login-right {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: flex-end;
          padding: 40px 64px 40px 24px;
          position: relative;
          z-index: 1;
        }
        .login-card {
          width: 100%;
          max-width: 400px;
          padding: 36px 32px 24px;
          box-shadow: 0 4px 20px rgba(0,0,0,0.1);
        }
        .login-card h2 {
          margin: 0 0 8px;
          font-size: 22px;
        }
        .login-sub {
          margin: 0 0 28px;
          color: var(--text-muted);
          font-size: 14px;
        }
        .login-field {
          position: relative;
          margin-bottom: 16px;
        }
        .field-icon {
          position: absolute;
          left: 12px;
          top: 50%;
          transform: translateY(-50%);
          color: var(--text-muted);
        }
        .login-input {
          width: 100%;
          padding: 12px 12px 12px 42px;
          border: 1px solid var(--border);
          border-radius: 8px;
          font-size: 14px;
          outline: none;
        }
        .login-input.no-pad { padding-right: 12px; }
        .login-input:focus { border-color: var(--primary); }
        .login-row-code {
          display: flex;
          gap: 10px;
          margin-bottom: 24px;
        }
        .flex1 { flex: 1; margin-bottom: 0; }
        .btn-code {
          flex-shrink: 0;
          padding: 0 16px;
          border: 1px solid var(--primary);
          color: var(--primary);
          background: #fff;
          border-radius: 8px;
          font-size: 13px;
          white-space: nowrap;
        }
        .login-submit {
          width: 100%;
          padding: 14px;
          font-size: 16px;
          text-decoration: none;
          margin-bottom: 24px;
        }
        .login-or {
          display: flex;
          align-items: center;
          gap: 12px;
          color: var(--text-muted);
          font-size: 12px;
          margin-bottom: 16px;
        }
        .login-or::before,
        .login-or::after {
          content: '';
          flex: 1;
          height: 1px;
          background: var(--border);
        }
        .wechat-wrap {
          display: flex;
          justify-content: center;
          margin-bottom: 16px;
        }
        .wechat-btn {
          width: 44px;
          height: 44px;
          border-radius: 50%;
          background: #07c160;
          display: block;
          cursor: pointer;
        }
        .login-alt {
          text-align: center;
          margin: 0 0 20px;
          font-size: 14px;
        }
        .mode-switch {
          border: none;
          background: transparent;
          color: #1890ff;
          font-size: 14px;
        }
        .login-msg {
          margin: -10px 0 14px;
          color: var(--text-secondary);
          font-size: 12px;
          text-align: center;
        }
        .login-footer {
          background: #fafafa;
          margin: 0 -32px -24px;
          padding: 16px;
          text-align: center;
          font-size: 13px;
          color: var(--text-muted);
          border-radius: 0 0 var(--radius-lg) var(--radius-lg);
        }
        @media (max-width: 900px) {
          .login-page { flex-direction: column; }
          .login-right { justify-content: center; padding: 24px; }
          .login-left { max-width: none; padding: 32px 24px 0; }
        }
      `}</style>
    </div>
  );
}
