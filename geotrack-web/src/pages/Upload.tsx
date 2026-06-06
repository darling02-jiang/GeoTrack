import { Link } from 'react-router-dom';
import { UserHeader } from '../components/UserHeader';
import {
  Check,
  CloudUpload,
  HelpCircle,
  Image as ImageIcon,
  Shield,
  Trash2,
} from 'lucide-react';
import { useState } from 'react';
import { useGeoTrack } from '../store/GeoTrackContext';

const previews = [
  { name: 'IMG_20240520_101500.jpg', size: '2.45 MB', wh: '4000 × 3000', status: '审核中', tag: 'success' },
  { name: 'IMG_20240520_101502.jpg', size: '1.12 MB', wh: '3000 × 2000', status: '待审核', tag: 'info' },
  { name: 'IMG_20240520_101505.jpg', size: '3.20 MB', wh: '4000 × 3000', status: '等待上传', tag: 'muted' },
];

const steps = [
  '获取上传凭证：申请临时凭证，支持客户端直传。',
  '直传 OSS/MinIO：对象存储承接大文件，降低服务端压力。',
  '异步压缩处理：后台生成多规格缩略图与 WebP。',
  '内容审核：安全合规检测，拦截违规素材。',
  '生成访问地址：审核通过后签发可访问 URL。',
];

export function Upload() {
  const [name, setName] = useState('IMG_20240520_101500.jpg');
  const [md5, setMd5] = useState('mock-md5-demo-001');
  const [msg, setMsg] = useState('');
  const { state, saveUpload } = useGeoTrack();

  const onSave = () => {
    const result = saveUpload(name, md5);
    setMsg(result.message);
  };

  return (
    <div className="page-shell">
      <UserHeader />
      <div className="page-main upload-page">
        <div className="upload-head">
          <div>
            <h1>图片上传与内容处理</h1>
            <p className="sub">图片将在审核通过后用于打卡展示与动态发布</p>
          </div>
          <button type="button" className="btn btn-outline">
            <HelpCircle size={16} /> 上传指南
          </button>
        </div>

        <div className="upload-cols">
          <div className="card col">
            <div className="drop-zone">
              <CloudUpload size={40} className="dz-ic" />
              <p>拖拽图片到此上传 或</p>
              <button type="button" className="btn btn-primary">
                选择图片
              </button>
              <p className="hint">支持 JPG / PNG 格式，单张不超过 10MB</p>
              <input className="search-inp" value={name} onChange={(e) => setName(e.target.value)} placeholder="模拟文件名" />
              <input className="search-inp" value={md5} onChange={(e) => setMd5(e.target.value)} placeholder="模拟 MD5" />
              <button type="button" className="btn btn-outline" onClick={onSave}>
                提交上传
              </button>
              {msg ? <p className="hint">{msg}</p> : null}
            </div>
            <div className="prog-block">
              <div className="prog-label">
                <span>上传进度</span>
                <span>2 / 3</span>
              </div>
              <div className="prog-bar">
                <div className="prog-in" style={{ width: '67%' }} />
              </div>
            </div>
            <div className="dedup card inner">
              <div className="dedup-row">
                <Shield size={18} className="text-ok" />
                <div>
                  <strong>图片 MD5 去重检测</strong>
                  <span className="tag tag-success" style={{ marginLeft: 8 }}>
                    未重复，可上传
                  </span>
                  <p className="dedup-sub">基于文件 MD5 与历史库比对</p>
                </div>
                <Check className="text-ok" size={22} />
              </div>
            </div>
          </div>

          <div className="card col">
            <h3 className="col-title">
              <ImageIcon size={18} /> 图片预览（{state.uploads.length} 张）
            </h3>
            <ul className="prev-list">
              {[...state.uploads, ...previews.map((p, idx) => ({ ...p, id: `mock-${idx}` }))].slice(0, 6).map((p: any) => (
                <li key={p.name} className="prev-item">
                  <div className="prev-thumb" />
                  <div className="prev-meta">
                    <strong>{p.name}</strong>
                    <div className="prev-sub">
                      {p.size} · {p.wh}
                    </div>
                    <span className={`tag tag-${p.tag}`}>{p.status}</span>
                  </div>
                  <button type="button" className="icon-del" aria-label="删除">
                    <Trash2 size={18} />
                  </button>
                </li>
              ))}
            </ul>
            <p className="foot-hint">最多可上传 9 张图片，支持拖拽调整顺序</p>
          </div>

          <div className="card col">
            <h3 className="col-title">上传流程说明</h3>
            <ol className="step-list">
              {steps.map((s, i) => (
                <li key={i}>
                  <span className="sn">{i + 1}</span>
                  <p>{s}</p>
                </li>
              ))}
            </ol>
            <div className="safe-tip">
              <Shield size={18} />
              <div>
                请勿上传违法违规内容，详见
                <a href="#std">《平台内容规范》</a>
              </div>
            </div>
          </div>
        </div>

        <div className="upload-actions">
          <Link to={`/poi/${state.pois[0]?.id || ''}`} className="btn btn-primary btn-lg">
            <Check size={20} /> 确认用于本次打卡
          </Link>
          <p className="disclaimer">确认后将用于生成打卡记录，提交后不可更换</p>
        </div>
      </div>
      <style>{`
        .upload-page .upload-head {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 16px;
          margin-bottom: 20px;
        }
        .upload-page h1 { margin: 0 0 8px; font-size: 22px; }
        .upload-page .sub { margin: 0; color: var(--text-muted); font-size: 14px; }
        .upload-cols {
          display: grid;
          grid-template-columns: 1fr 1fr 280px;
          gap: 16px;
          align-items: start;
        }
        @media (max-width: 1100px) {
          .upload-cols { grid-template-columns: 1fr; }
        }
        .upload-cols .col { padding: 20px; }
        .drop-zone {
          border: 2px dashed var(--primary);
          border-radius: var(--radius-lg);
          padding: 32px 20px;
          text-align: center;
          background: var(--primary-light);
          margin-bottom: 16px;
        }
        .dz-ic { color: var(--primary); margin-bottom: 12px; }
        .drop-zone p { margin: 8px 0; color: var(--text-secondary); }
        .hint { font-size: 12px; color: var(--text-muted); margin-top: 12px !important; }
        .prog-block { margin-bottom: 16px; }
        .prog-label {
          display: flex;
          justify-content: space-between;
          font-size: 13px;
          margin-bottom: 8px;
        }
        .prog-bar {
          height: 8px;
          background: #e6f7f5;
          border-radius: 4px;
          overflow: hidden;
        }
        .prog-in {
          height: 100%;
          background: linear-gradient(90deg, #26b6a7, #1da1f2);
          border-radius: 4px;
        }
        .inner { padding: 14px; box-shadow: none; border: 1px solid var(--border); }
        .dedup-row {
          display: flex;
          align-items: flex-start;
          gap: 10px;
        }
        .dedup-sub { margin: 6px 0 0; font-size: 12px; color: var(--text-muted); }
        .text-ok { color: var(--success); flex-shrink: 0; }
        .col-title {
          margin: 0 0 16px;
          font-size: 15px;
          display: flex;
          align-items: center;
          gap: 8px;
        }
        .prev-list { list-style: none; margin: 0; padding: 0; }
        .prev-item {
          display: grid;
          grid-template-columns: 64px 1fr auto;
          gap: 12px;
          align-items: center;
          padding: 10px 0;
          border-bottom: 1px solid var(--border);
        }
        .prev-item:last-child { border-bottom: none; }
        .prev-thumb {
          width: 64px;
          height: 48px;
          border-radius: 8px;
          background: linear-gradient(135deg, #a5b4fc, #818cf8);
        }
        .prev-meta strong { font-size: 13px; }
        .prev-sub { font-size: 12px; color: var(--text-muted); margin: 4px 0 6px; }
        .icon-del {
          background: none;
          border: none;
          color: var(--danger);
          padding: 8px;
          cursor: pointer;
        }
        .foot-hint { font-size: 12px; color: var(--text-muted); margin: 12px 0 0; }
        .step-list {
          margin: 0;
          padding: 0;
          list-style: none;
        }
        .step-list li {
          display: flex;
          gap: 12px;
          margin-bottom: 14px;
          font-size: 13px;
          color: var(--text-secondary);
          line-height: 1.5;
        }
        .sn {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: var(--primary-light);
          color: var(--primary);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          font-weight: 700;
          flex-shrink: 0;
        }
        .step-list p { margin: 0; }
        .safe-tip {
          margin-top: 16px;
          padding: 12px;
          background: #f6ffed;
          border: 1px solid #b7eb8f;
          border-radius: 8px;
          font-size: 12px;
          display: flex;
          gap: 10px;
          align-items: flex-start;
          color: var(--text-secondary);
        }
        .upload-actions {
          text-align: center;
          margin-top: 32px;
        }
        .btn-lg {
          padding: 14px 40px;
          font-size: 16px;
          text-decoration: none;
        }
        .disclaimer {
          margin: 12px 0 0;
          font-size: 12px;
          color: var(--text-muted);
        }
      `}</style>
    </div>
  );
}
