import { UserHeader, CategoryScroll } from '../components/UserHeader';
import { Flame, Heart, MessageCircle, RefreshCw, Share2, Star } from 'lucide-react';
import { useState } from 'react';
import { useGeoTrack } from '../store/GeoTrackContext';

export function FeedCircle() {
  const {
    state,
    toggleLike,
    commentPost,
    setFeedCirclePoi,
    setFeedCircleSort,
    refreshFeedCircle,
  } = useGeoTrack();
  const [commenting, setCommenting] = useState<Record<string, string>>({});
  const [busyLike, setBusyLike] = useState<Record<string, boolean>>({});

  const enabledPois = state.pois.filter((p) => p.status === 'enabled');
  const currentPoiName =
    enabledPois.find((p) => p.id === state.feedCirclePoiId)?.name ?? '未选择';

  const totalLikes = state.posts.reduce((s, p) => s + p.likes, 0);
  const totalComments = state.posts.reduce((s, p) => s + p.comments.length, 0);

  return (
    <div className="page-shell">
      <UserHeader />
      <div className="page-main">
        <CategoryScroll />
        <div className="feed-layout">
          <div className="feed-main">
            <div className="card feed-toolbar">
              <div className="feed-toolbar-row">
                <label className="feed-field">
                  <span>圈子 POI</span>
                  <select
                    className="search-inp"
                    value={state.feedCirclePoiId}
                    onChange={(e) => void setFeedCirclePoi(e.target.value)}
                  >
                    {enabledPois.length === 0 ? (
                      <option value="">暂无可用 POI</option>
                    ) : (
                      enabledPois.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.name}
                        </option>
                      ))
                    )}
                  </select>
                </label>
                <div className="toggle-row feed-sort">
                  <button
                    type="button"
                    className={state.feedCircleSort === 'latest' ? 'on' : ''}
                    onClick={() => void setFeedCircleSort('latest')}
                  >
                    最新
                  </button>
                  <button
                    type="button"
                    className={state.feedCircleSort === 'likes' ? 'on' : ''}
                    onClick={() => void setFeedCircleSort('likes')}
                  >
                    点赞最多
                  </button>
                </div>
                <button
                  type="button"
                  className="btn btn-outline feed-refresh"
                  onClick={() => void refreshFeedCircle()}
                  title="刷新"
                >
                  <RefreshCw size={16} /> 刷新
                </button>
              </div>
            </div>

            {!state.feedCirclePoiId ? (
              <div className="card post-card muted-card">请先选择或等待加载 POI。</div>
            ) : state.posts.length === 0 ? (
              <div className="card post-card muted-card">该 POI 下暂无动态。</div>
            ) : (
              state.posts.map((p) => (
                <article key={p.id} className="card post-card">
                  <div className="post-head">
                    <span className="avatar-md" />
                    <div>
                      <div className="post-user">
                        <strong>{p.userName}</strong>
                      </div>
                      <div className="post-meta">{p.createdAt}</div>
                    </div>
                    <span className="place-tag">{p.poiName}</span>
                  </div>
                  <div className="post-body">
                    <div
                      className="post-img"
                      style={
                        p.images[0]
                          ? {
                              backgroundImage: `url(${p.images[0]})`,
                              backgroundSize: 'cover',
                              backgroundPosition: 'center',
                            }
                          : undefined
                      }
                    />
                    <div className="post-text-col">
                      <p>{p.text}</p>
                      <div className="reward-box">
                        <Star size={14} /> 打卡动态
                      </div>
                      {p.comments.length > 0 ? (
                        <div className="comment-preview">
                          {p.comments.slice(0, 3).map((c) => (
                            <div key={c.id} className="comment-line">
                              <strong>{c.user}</strong>：{c.text}
                            </div>
                          ))}
                        </div>
                      ) : null}
                    </div>
                  </div>
                  <div className="post-foot">
                    <span
                      className={!state.token ? 'disabled' : ''}
                      onClick={() => {
                        if (!state.token || busyLike[p.id]) return;
                        setBusyLike((prev) => ({ ...prev, [p.id]: true }));
                        void toggleLike(p.id).finally(() => {
                          setBusyLike((prev) => ({ ...prev, [p.id]: false }));
                        });
                      }}
                    >
                      <Heart size={18} /> {p.likes}
                    </span>
                    <span>
                      <MessageCircle size={18} /> {p.comments.length}
                    </span>
                    <span>
                      <Share2 size={18} />
                    </span>
                  </div>
                  <div style={{ marginTop: 10, display: 'flex', gap: 8 }}>
                    <input
                      className="search-inp"
                      placeholder={state.token ? '输入评论' : '登录后可评论'}
                      value={commenting[p.id] || ''}
                      disabled={!state.token}
                      onChange={(e) => setCommenting((prev) => ({ ...prev, [p.id]: e.target.value }))}
                    />
                    <button
                      type="button"
                      className="btn btn-outline"
                      disabled={!state.token}
                      onClick={() => {
                        const text = commenting[p.id] || '';
                        void commentPost(p.id, text).then((res) => {
                          if (res.ok) setCommenting((prev) => ({ ...prev, [p.id]: '' }));
                        });
                      }}
                    >
                      评论
                    </button>
                  </div>
                </article>
              ))
            )}
          </div>
          <aside className="feed-side">
            <div className="card side-block">
              <h4 className="side-title">热门动态 TOP10</h4>
              <p className="side-sub">按热度 · {currentPoiName}</p>
              <ol className="rank-list">
                {state.feedHotRank.length === 0 ? (
                  <li className="rank-empty">暂无榜单数据</li>
                ) : (
                  state.feedHotRank.map((item, i) => (
                    <li key={item.id}>
                      <span className="rn">{i + 1}</span>
                      <span className="r-thumb" />
                      <div className="r-info">
                        <div className="r-t">{item.title}</div>
                        <div className="r-a">作者：{item.author}</div>
                      </div>
                      <span className="heat">
                        <Flame size={12} /> {item.heatLabel}
                      </span>
                    </li>
                  ))
                )}
              </ol>
            </div>
            <div className="card side-block">
              <h4 className="side-title">圈子统计 · {currentPoiName}</h4>
              <div className="stat-grid">
                <div>
                  <div className="sg-n">{state.posts.length}</div>
                  <div className="sg-l">动态</div>
                </div>
                <div>
                  <div className="sg-n">{totalComments}</div>
                  <div className="sg-l">评论（预览条数）</div>
                </div>
                <div>
                  <div className="sg-n">{totalLikes}</div>
                  <div className="sg-l">点赞</div>
                </div>
                <div>
                  <div className="sg-n">—</div>
                  <div className="sg-l">积分</div>
                </div>
              </div>
            </div>
            <div className="card side-block">
              <div className="side-head-r">
                <h4 className="side-title" style={{ margin: 0 }}>
                  热门 POI 推荐
                </h4>
              </div>
              <div className="poi-mini-row">
                {enabledPois.slice(0, 6).map((poi) => (
                  <button
                    key={poi.id}
                    type="button"
                    className="poi-mini poi-mini-btn"
                    onClick={() => void setFeedCirclePoi(poi.id)}
                  >
                    <div className="poi-mini-img" />
                    <div className="poi-mini-name">{poi.name}</div>
                  </button>
                ))}
              </div>
            </div>
          </aside>
        </div>
      </div>
      <style>{`
        .feed-toolbar { padding: 16px; margin-bottom: 16px; }
        .feed-toolbar-row {
          display: flex;
          flex-wrap: wrap;
          align-items: flex-end;
          gap: 12px;
        }
        .feed-field {
          display: flex;
          flex-direction: column;
          gap: 6px;
          font-size: 12px;
          color: var(--text-muted);
          min-width: 180px;
        }
        .feed-field select { min-width: 200px; }
        .feed-sort.toggle-row { flex: 1; min-width: 200px; margin-bottom: 0; }
        .feed-refresh {
          display: inline-flex;
          align-items: center;
          gap: 6px;
        }
        .muted-card {
          padding: 24px;
          text-align: center;
          color: var(--text-muted);
        }
        .comment-preview {
          margin-top: 10px;
          padding: 8px 10px;
          background: #f8fafc;
          border-radius: 8px;
          font-size: 12px;
          color: var(--text-secondary);
        }
        .comment-line { margin: 4px 0; }
        .post-foot span.disabled { opacity: 0.5; cursor: not-allowed; }
        .side-sub {
          margin: 0 0 12px;
          font-size: 12px;
          color: var(--text-muted);
        }
        .rank-empty {
          list-style: none;
          padding: 12px 0;
          color: var(--text-muted);
          font-size: 13px;
        }
        .poi-mini-btn {
          border: none;
          background: transparent;
          padding: 0;
          cursor: pointer;
          text-align: center;
        }
        .feed-layout {
          display: grid;
          grid-template-columns: 1fr 320px;
          gap: 20px;
          align-items: start;
        }
        @media (max-width: 1024px) {
          .feed-layout { grid-template-columns: 1fr; }
        }
        .post-card { padding: 20px; margin-bottom: 20px; }
        .post-head {
          display: flex;
          align-items: flex-start;
          gap: 12px;
          margin-bottom: 16px;
          position: relative;
        }
        .avatar-md {
          width: 44px;
          height: 44px;
          border-radius: 50%;
          background: linear-gradient(135deg, #94a3b8, #64748b);
          flex-shrink: 0;
        }
        .post-user { display: flex; align-items: center; gap: 8px; }
        .post-user strong { font-size: 15px; }
        .post-meta { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
        .place-tag {
          margin-left: auto;
          padding: 4px 10px;
          background: #f0fdfa;
          color: var(--primary);
          border-radius: 999px;
          font-size: 12px;
        }
        .post-body {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 16px;
        }
        @media (max-width: 700px) {
          .post-body { grid-template-columns: 1fr; }
        }
        .post-img {
          min-height: 180px;
          border-radius: 12px;
          background: linear-gradient(135deg, #99f6e4, #5eead4);
        }
        .post-text-col p {
          margin: 0 0 12px;
          font-size: 14px;
          line-height: 1.6;
          color: var(--text-secondary);
        }
        .reward-box {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 8px 12px;
          background: #f6ffed;
          border: 1px solid #b7eb8f;
          border-radius: 8px;
          font-size: 13px;
          color: var(--success);
          font-weight: 600;
        }
        .post-foot {
          display: flex;
          gap: 24px;
          margin-top: 16px;
          padding-top: 12px;
          border-top: 1px solid var(--border);
          color: var(--text-muted);
          font-size: 14px;
        }
        .post-foot span {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          cursor: pointer;
        }
        .side-block { padding: 16px; margin-bottom: 16px; }
        .toggle-row {
          display: flex;
          gap: 8px;
          margin-bottom: 12px;
        }
        .toggle-row button {
          flex: 1;
          padding: 8px;
          border: 1px solid var(--border);
          background: #fff;
          border-radius: 8px;
          font-size: 12px;
          cursor: pointer;
        }
        .toggle-row button.on {
          background: var(--primary);
          color: #fff;
          border-color: var(--primary);
        }
        .side-title { margin: 0 0 12px; font-size: 15px; }
        .rank-list { list-style: none; margin: 0; padding: 0; }
        .rank-list li {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 0;
          border-bottom: 1px solid var(--border);
          font-size: 12px;
        }
        .rn { font-weight: 700; color: var(--primary); width: 18px; }
        .r-thumb { width: 36px; height: 36px; border-radius: 6px; background: #e2e8f0; }
        .r-info { flex: 1; min-width: 0; }
        .r-t { font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .r-a { color: var(--text-muted); }
        .heat { color: #fa8c16; display: flex; align-items: center; gap: 2px; }
        .stat-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 12px;
          text-align: center;
        }
        .sg-n { font-size: 18px; font-weight: 700; }
        .sg-l { font-size: 12px; color: var(--text-muted); }
        .side-head-r { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
        .poi-mini-row { display: flex; gap: 10px; overflow-x: auto; }
        .poi-mini { flex: 0 0 72px; text-align: center; font-size: 11px; }
        .poi-mini-img { height: 96px; border-radius: 8px; background: linear-gradient(180deg, #bae6fd, #7dd3fc); margin-bottom: 6px; }
        .poi-mini-name { font-weight: 500; }
      `}</style>
    </div>
  );
}
