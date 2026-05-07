import { useState, useEffect, useCallback } from 'react';
import './App.css';

// ── Sabitler ─────────────────────────────────────────────────────────────────
const DAYS   = ['Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma'];
const HOURS  = ['08:00', '09:00', '10:00', '11:00', '12:00',
                '13:00', '14:00', '15:00', '16:00', '17:00'];
const API    = 'http://localhost:8080/api/schedule';

function deptClass(deptName = '') {
  if (!deptName) return 'dept-other';
  const n = deptName.toLowerCase();
  if (n.includes('bilgisayar') || n.includes('cs'))   return 'dept-cs';
  if (n.includes('matematik')  || n.includes('math')) return 'dept-math';
  if (n.includes('fizik')      || n.includes('phy'))  return 'dept-phy';
  return 'dept-other';
}

// ── Ana Bileşen ───────────────────────────────────────────────────────────────
function App() {
  const [schedule,     setSchedule]     = useState([]);
  const [unscheduled,  setUnscheduled]  = useState([]);
  const [departments,  setDepartments]  = useState([]);
  const [stats,        setStats]        = useState(null);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState(null);
  const [viewMode,     setViewMode]     = useState('weekly');
  const [deptFilter,   setDeptFilter]   = useState('all');
  const [elapsed,      setElapsed]      = useState(null);

  // Simülasyon State'leri
  const [simCourseId,  setSimCourseId]  = useState('');
  const [simType,      setSimType]      = useState('enrollment');
  const [isSimulating, setIsSimulating] = useState(false);

  // ── Veri Yükleme ──────────────────────────────────────────────────────────
  const fetchSchedule = useCallback(async (deptId = null) => {
    setLoading(true);
    setError(null);
    const t0 = Date.now();
    try {
      const url = deptId && deptId !== 'all'
        ? `${API}/generate/department/${deptId}`
        : `${API}/generate`;
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      // API artık { scheduled, unscheduled, ... } döndürüyor
      setSchedule(data.scheduled   || []);
      setUnscheduled(data.unscheduled || []);
      setElapsed(Date.now() - t0);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  // ── Simülasyon Tetikleyici ──────────────────────────────────────────────
  const handleSimulation = async () => {
    if (!simCourseId) return;
    setIsSimulating(true);
    try {
      const res = await fetch(`${API}/update`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: simType,
          courseId: parseInt(simCourseId),
          entityId: 0
        })
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      await fetchSchedule(deptFilter === 'all' ? null : deptFilter);
    } catch (e) {
      alert('Simülasyon başarısız: ' + e.message);
    } finally {
      setIsSimulating(false);
    }
  };

  const uniqueCourses = Array.from(new Map(schedule.map(sc => [sc.courseId, sc])).values());

  useEffect(() => {
    fetch(`${API}/departments`).then(r => r.json()).then(setDepartments).catch(() => {});
    fetch(`${API}/stats`).then(r => r.json()).then(setStats).catch(() => {});
    fetchSchedule();
  }, [fetchSchedule]);

  const handleDeptChange = (e) => {
    setDeptFilter(e.target.value);
    fetchSchedule(e.target.value === 'all' ? null : e.target.value);
  };

  // ── Render ──────────────────────────────────────────────────────────────────
  return (
    <>
      {/* ── Header ── */}
      <header className="header">
        <div className="header-left">
          <h1>🎓 Academic Scheduler</h1>
          <p>O(1) Matrix · Critical Path · Department-Aware · H2 Database</p>
        </div>
        <div className="header-right">
          {stats && (
            <>
              <div className="stat-pill">📚 <span>{stats.totalCourses}</span> Ders</div>
              <div className="stat-pill">🏛️ <span>{stats.totalDepartments}</span> Departman</div>
              <div className="stat-pill">👥 <span>{stats.totalEnrollments}</span> Kayıt</div>
            </>
          )}
        </div>
      </header>

      {/* ── Controls ── */}
      <div className="controls">
        <button
          className={`btn ${viewMode === 'weekly' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setViewMode('weekly')}
        >
          📅 Haftalık Tablo
        </button>
        <button
          className={`btn ${viewMode === 'cards' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setViewMode('cards')}
        >
          🗂️ Kart Görünümü
        </button>

        <select className="dept-filter" value={deptFilter} onChange={handleDeptChange}>
          <option value="all">Tüm Departmanlar</option>
          {departments.map(d => (
            <option key={d.id} value={d.id}>{d.name}</option>
          ))}
        </select>

        <button className="btn btn-secondary" onClick={() => fetchSchedule(deptFilter === 'all' ? null : deptFilter)}>
          🔄 Yeniden Üret
        </button>

        {elapsed !== null && (
          <div className="elapsed-badge">
            ⚡ {elapsed} ms'de tamamlandı
          </div>
        )}

        {/* Simülasyon paneli */}
        <div className="simulation-panel">
          <span className="sim-label">⚡ Simülasyon:</span>
          <select value={simCourseId} onChange={e => setSimCourseId(e.target.value)} className="sim-select">
            <option value="">-- Ders Seç --</option>
            {uniqueCourses.map(c => (
              <option key={c.courseId} value={c.courseId}>{c.courseName}</option>
            ))}
          </select>
          <select value={simType} onChange={e => setSimType(e.target.value)} className="sim-select">
            <option value="enrollment">+ Öğrenci Ekle</option>
            <option value="instructor">Eğitmen Değişimi</option>
          </select>
          <button className="btn btn-primary sim-btn" onClick={handleSimulation} disabled={isSimulating || !simCourseId}>
            {isSimulating ? '...' : 'Test Et'}
          </button>
        </div>
      </div>

      {/* ── Content ── */}
      <main className="content">

        {/* Legend */}
        {!loading && !error && (
          <div className="legend">
            <div className="legend-item">
              <div className="legend-dot" style={{background:'#3b82f6'}}/>
              <span>Bilgisayar Müh.</span>
            </div>
            <div className="legend-item">
              <div className="legend-dot" style={{background:'#8b5cf6'}}/>
              <span>Matematik</span>
            </div>
            <div className="legend-item">
              <div className="legend-dot" style={{background:'#10b981'}}/>
              <span>Fizik</span>
            </div>
            <div className="legend-item">
              <div className="legend-dot" style={{background:'#f59e0b', borderRadius:'50%'}}/>
              <span>🔥 Critical Path</span>
            </div>
            <div className="legend-item">
              <div className="legend-dot" style={{background:'#f97316', borderRadius:'2px', border:'2px solid #f97316'}}/>
              <span>⚠️ Kaydırılmış ders</span>
            </div>
            <div className="legend-item" style={{marginLeft:'auto', color:'#64748b'}}>
              Toplam: <strong style={{color:'#f1f5f9', marginLeft:4}}>{schedule.length} ders zamanlandı</strong>
              {unscheduled.length > 0 && (
                <strong style={{color:'#ef4444', marginLeft:8}}>· {unscheduled.length} zamanlanamadı ⚠️</strong>
              )}
            </div>
          </div>
        )}

        {/* Loading */}
        {loading && (
          <div className="status-center">
            <div className="spinner"/>
            <p>Çizelge hesaplanıyor…</p>
          </div>
        )}

        {/* Error */}
        {!loading && error && (
          <div className="status-center">
            <p style={{color:'#ef4444', fontSize:'1.1rem'}}>⚠️ {error}</p>
            <p style={{fontSize:'0.85rem'}}>Backend'in çalıştığından emin ol: <code>mvn spring-boot:run</code></p>
          </div>
        )}

        {/* Zamanlanamayan Dersler Uyarı Paneli */}
        {!loading && !error && unscheduled.length > 0 && (
          <div className="unscheduled-panel">
            <div className="unscheduled-header">
              ❌ Zamanlanamayan Dersler ({unscheduled.length})
            </div>
            <div className="unscheduled-list">
              {unscheduled.map((uc, i) => (
                <div key={i} className="unscheduled-item">
                  <span className="unscheduled-name">{uc.courseName || `Ders ${uc.courseId}`}</span>
                  <span className="unscheduled-dept">{uc.departmentName || '—'}</span>
                  <span className="unscheduled-reason">💬 {uc.failReason}</span>
                  <span className="unscheduled-enroll">👤 {uc.enrollmentCount} kayıt</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Weekly Table */}
        {!loading && !error && viewMode === 'weekly' && (
          <WeeklyTable schedule={schedule} />
        )}

        {/* Card Grid */}
        {!loading && !error && viewMode === 'cards' && (
          <CardGrid schedule={schedule} />
        )}
      </main>
    </>
  );
}

// ── Haftalık Tablo Bileşeni ───────────────────────────────────────────────────
function WeeklyTable({ schedule }) {
  const grid = (() => {
    const g = Array.from({length: 5}, () => Array(10).fill(null));
    schedule.forEach(sc => {
      const slot = sc.timeSlotId;
      const day  = Math.floor(slot / 10);
      const hour = slot % 10;
      if (day < 5 && hour < 10) g[day][hour] = sc;
    });
    return g;
  })();

  return (
    <div className="table-wrapper">
      <table className="weekly-table">
        <thead>
          <tr>
            <th>Saat</th>
            {DAYS.map(d => <th key={d}>{d}</th>)}
          </tr>
        </thead>
        <tbody>
          {HOURS.map((hour, hIdx) => (
            <tr key={hour}>
              <td className="time-col">{hour}</td>
              {DAYS.map((_, dIdx) => {
                const sc = grid[dIdx][hIdx];
                if (!sc) return (
                  <td key={dIdx} className="cell cell-empty"/>
                );
                const isCritical = sc.criticalPathLength >= 3;
                const isShifted  = sc.wasShifted;
                return (
                  <td key={dIdx} className="cell">
                    <div className={`course-card ${deptClass(sc.departmentName)} ${isShifted ? 'card-shifted' : ''}`}
                         title={sc.resolutionReason || ''}>
                      <div>
                        <div className="course-name">
                          {sc.courseName || `Ders ${sc.courseId}`}
                          {isShifted && <span className="shifted-icon" title={sc.resolutionReason}>⚠️</span>}
                          {!isShifted && sc.resolutionReason && <span className="info-icon" title={sc.resolutionReason}>ℹ️</span>}
                        </div>
                        <div className="course-code">🏛️ {sc.departmentName || '—'}</div>
                      </div>
                      <div className="course-meta">
                        <span className="enrollment-badge">👤 {sc.enrollmentCount}</span>
                        {isCritical && (
                          <span className="critical-badge">🔥 CP:{sc.criticalPathLength}</span>
                        )}
                      </div>
                    </div>
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ── Kart Grid Bileşeni ────────────────────────────────────────────────────────
function CardGrid({ schedule }) {
  if (schedule.length === 0)
    return <div className="status-center"><p>Zamanlanmış ders yok.</p></div>;

  return (
    <div className="card-grid">
      {schedule.map((sc, idx) => {
        const isCritical = sc.criticalPathLength >= 3;
        return (
          <div key={idx} className={`card-item ${deptClass(sc.departmentName)} ${sc.wasShifted ? 'card-item-shifted' : ''}`}>
            <div className="card-item-header">
              <span style={{fontWeight:700, fontSize:'0.9rem'}}>
                {sc.courseName || `Ders ${sc.courseId}`}
                {sc.wasShifted && <span style={{marginLeft:6, fontSize:'0.75rem', color:'#f97316'}}>⚠️ Kaydırıldı</span>}
              </span>
              {isCritical && <span className="critical-badge">🔥 CP</span>}
            </div>
            <div className="card-item-body">
              <div>📅 <strong>{sc.dayName}</strong> {sc.hourLabel}</div>
              <div>🏛️ <strong>{sc.departmentName || '—'}</strong></div>
              <div>🚪 Oda {sc.roomId + 1} ({sc.roomCapacity} kişilik)</div>
              <div>👥 {sc.enrollmentCount} kayıt</div>
              <div>🔗 Kritik Yol: <strong>{sc.criticalPathLength}</strong></div>
              {sc.resolutionReason && (
                <div style={{fontSize:'0.7rem', color:'#94a3b8', marginTop:4}}>
                  💬 {sc.resolutionReason}
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default App;