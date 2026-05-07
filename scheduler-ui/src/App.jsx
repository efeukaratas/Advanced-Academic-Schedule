import { useState, useEffect, useCallback } from 'react';
import { Routes, Route, NavLink } from 'react-router-dom';
import CoursesPage from './pages/CoursesPage';
import InstructorsPage from './pages/InstructorsPage';
import RoomsPage from './pages/RoomsPage';
import StudentsPage from './pages/StudentsPage';
import EnrollmentsPage from './pages/EnrollmentsPage';
import PreferencesPage from './pages/PreferencesPage';
import './App.css';

const DAYS  = ['Pazartesi', 'Sali', 'Carsamba', 'Persembe', 'Cuma'];
const HOURS = ['08:00','09:00','10:00','11:00','12:00','13:00','14:00','15:00','16:00','17:00'];
const API   = 'http://localhost:8080/api/schedule';

function deptClass(d = '') {
  if (!d) return 'dept-other';
  const n = d.toLowerCase();
  if (n.includes('bilgisayar') || n.includes('cs'))   return 'dept-cs';
  if (n.includes('matematik')  || n.includes('math')) return 'dept-math';
  if (n.includes('fizik')      || n.includes('phy'))  return 'dept-phy';
  return 'dept-other';
}

// ── Layout ─────────────────────────────────────────────────────────────────────
function App() {
  const [stats, setStats] = useState(null);
  useEffect(() => { fetch(`${API}/stats`).then(r=>r.json()).then(setStats).catch(()=>{}); }, []);

  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1>Academic Scheduler</h1>
          <p>CSP · Backtracking · Critical Path · Persistent DB</p>
        </div>
        <div className="header-right">
          {stats && (
            <>
              <div className="stat-pill"><span>{stats.totalCourses}</span> Ders</div>
              <div className="stat-pill"><span>{stats.totalDepartments}</span> Dept</div>
              <div className="stat-pill"><span>{stats.totalInstructors||'--'}</span> Egitmen</div>
              <div className="stat-pill"><span>{stats.totalRooms||'--'}</span> Oda</div>
            </>
          )}
        </div>
      </header>

      <nav className="nav-bar">
        <NavLink to="/" end className={({isActive}) => `nav-link ${isActive?'nav-active':''}`}>Cizelge</NavLink>
        <NavLink to="/courses"     className={({isActive}) => `nav-link ${isActive?'nav-active':''}`}>Dersler</NavLink>
        <NavLink to="/instructors" className={({isActive}) => `nav-link ${isActive?'nav-active':''}`}>Egitmenler</NavLink>
        <NavLink to="/rooms"       className={({isActive}) => `nav-link ${isActive?'nav-active':''}`}>Odalar</NavLink>
        <NavLink to="/students"    className={({isActive}) => `nav-link ${isActive?'nav-active':''}`}>Ogrenciler</NavLink>
        <NavLink to="/enrollments" className={({isActive}) => `nav-link ${isActive?'nav-active':''}`}>Kayitlar</NavLink>
        <NavLink to="/preferences" className={({isActive}) => `nav-link ${isActive?'nav-active':''}`}>Tercihler</NavLink>
      </nav>

      <main className="content">
        <Routes>
          <Route path="/" element={<SchedulePage />} />
          <Route path="/courses" element={<CoursesPage />} />
          <Route path="/instructors" element={<InstructorsPage />} />
          <Route path="/rooms" element={<RoomsPage />} />
          <Route path="/students" element={<StudentsPage />} />
          <Route path="/enrollments" element={<EnrollmentsPage />} />
          <Route path="/preferences" element={<PreferencesPage />} />
        </Routes>
      </main>
    </>
  );
}

// ── Cizelge Sayfasi ────────────────────────────────────────────────────────────
function SchedulePage() {
  const [schedule,     setSchedule]     = useState([]);
  const [unscheduled,  setUnscheduled]  = useState([]);
  const [departments,  setDepartments]  = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState(null);
  const [viewMode,     setViewMode]     = useState('weekly');
  const [deptFilter,   setDeptFilter]   = useState('all');
  const [elapsed,      setElapsed]      = useState(null);
  const [simCourseId,  setSimCourseId]  = useState('');
  const [simType,      setSimType]      = useState('enrollment');
  const [isSimulating, setIsSimulating] = useState(false);

  const fetchSchedule = useCallback(async (deptId = null) => {
    setLoading(true); setError(null);
    const t0 = Date.now();
    try {
      const url = deptId && deptId !== 'all' ? `${API}/generate/department/${deptId}` : `${API}/generate`;
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setSchedule(data.scheduled || []);
      setUnscheduled(data.unscheduled || []);
      setElapsed(Date.now() - t0);
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, []);

  const handleSimulation = async () => {
    if (!simCourseId) return;
    setIsSimulating(true);
    try {
      const res = await fetch(`${API}/update`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({type:simType,courseId:+simCourseId,entityId:0}) });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      await fetchSchedule(deptFilter === 'all' ? null : deptFilter);
    } catch (e) { alert('Simulasyon basarisiz: ' + e.message); }
    finally { setIsSimulating(false); }
  };

  const uniqueCourses = Array.from(new Map(schedule.map(sc => [sc.courseId, sc])).values());
  useEffect(() => { fetch(`${API}/departments`).then(r=>r.json()).then(setDepartments).catch(()=>{}); fetchSchedule(); }, [fetchSchedule]);
  const handleDeptChange = (e) => { setDeptFilter(e.target.value); fetchSchedule(e.target.value === 'all' ? null : e.target.value); };

  return (
    <>
      <div className="controls">
        <button className={`btn ${viewMode==='weekly'?'btn-primary':'btn-secondary'}`} onClick={()=>setViewMode('weekly')}>Haftalik Tablo</button>
        <button className={`btn ${viewMode==='cards'?'btn-primary':'btn-secondary'}`} onClick={()=>setViewMode('cards')}>Kart Gorunumu</button>
        <select className="dept-filter" value={deptFilter} onChange={handleDeptChange}>
          <option value="all">Tum Departmanlar</option>
          {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        <button className="btn btn-secondary" onClick={()=>fetchSchedule(deptFilter==='all'?null:deptFilter)}>Yeniden Uret</button>
        {elapsed!==null && <div className="elapsed-badge">{elapsed} ms</div>}
        <div className="simulation-panel">
          <span className="sim-label">Simulasyon:</span>
          <select value={simCourseId} onChange={e=>setSimCourseId(e.target.value)} className="sim-select">
            <option value="">-- Ders Sec --</option>
            {uniqueCourses.map(c => <option key={c.courseId} value={c.courseId}>{c.courseName}</option>)}
          </select>
          <select value={simType} onChange={e=>setSimType(e.target.value)} className="sim-select">
            <option value="enrollment">+ Ogrenci</option>
            <option value="instructor">Egitmen Degisimi</option>
          </select>
          <button className="btn btn-primary sim-btn" onClick={handleSimulation} disabled={isSimulating||!simCourseId}>{isSimulating?'...':'Test Et'}</button>
        </div>
      </div>

      {!loading && !error && (
        <div className="legend">
          <div className="legend-item"><div className="legend-dot" style={{background:'#3b82f6'}}/><span>Bilgisayar Muh.</span></div>
          <div className="legend-item"><div className="legend-dot" style={{background:'#8b5cf6'}}/><span>Matematik</span></div>
          <div className="legend-item"><div className="legend-dot" style={{background:'#10b981'}}/><span>Fizik</span></div>
          <div className="legend-item"><div className="legend-dot critical-dot"/><span>Critical Path</span></div>
          <div className="legend-item"><div className="legend-dot shifted-dot"/><span>Kaydirilan</span></div>
          <div className="legend-item" style={{marginLeft:'auto',color:'#64748b'}}>
            Toplam: <strong style={{color:'#f1f5f9',marginLeft:4}}>{schedule.length} ders zamanlandi</strong>
            {unscheduled.length>0 && <strong style={{color:'#ef4444',marginLeft:8}}>| {unscheduled.length} basarisiz</strong>}
          </div>
        </div>
      )}

      {loading && <div className="status-center"><div className="spinner"/><p>Hesaplaniyor...</p></div>}
      {!loading && error && <div className="status-center"><p className="error-text">Hata: {error}</p></div>}

      {!loading && !error && unscheduled.length > 0 && (
        <div className="unscheduled-panel">
          <div className="unscheduled-header">Zamanlanamayan Dersler ({unscheduled.length})</div>
          <div className="unscheduled-list">
            {unscheduled.map((uc,i) => (
              <div key={i} className="unscheduled-item">
                <span className="unscheduled-name">{uc.courseName||`Ders ${uc.courseId}`}</span>
                <span className="unscheduled-dept">{uc.departmentName||'--'}</span>
                <span className="unscheduled-reason">{uc.failReason}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {!loading && !error && viewMode==='weekly' && <WeeklyTable schedule={schedule}/>}
      {!loading && !error && viewMode==='cards'  && <CardGrid schedule={schedule}/>}
    </>
  );
}

// ── Haftalik Tablo ──────────────────────────────────────────────────────────────
function WeeklyTable({ schedule }) {
  const grid = (() => {
    const g = Array.from({length:5}, ()=>Array(10).fill(null));
    schedule.forEach(sc => { const d=Math.floor(sc.timeSlotId/10), h=sc.timeSlotId%10; if(d<5&&h<10) g[d][h]=sc; });
    return g;
  })();
  return (
    <div className="table-wrapper">
      <table className="weekly-table">
        <thead><tr><th>Saat</th>{DAYS.map(d=><th key={d}>{d}</th>)}</tr></thead>
        <tbody>
          {HOURS.map((hour,hIdx) => (
            <tr key={hour}>
              <td className="time-col">{hour}</td>
              {DAYS.map((_,dIdx) => {
                const sc = grid[dIdx][hIdx];
                if (!sc) return <td key={dIdx} className="cell cell-empty"/>;
                const isCrit = sc.criticalPathLength >= 3;
                const shifted = sc.wasShifted;
                return (
                  <td key={dIdx} className="cell">
                    <div className={`course-card ${deptClass(sc.departmentName)} ${shifted?'card-shifted':''}`} title={sc.resolutionReason||''}>
                      <div>
                        <div className="course-name">
                          {sc.courseName||`Ders ${sc.courseId}`}
                          {shifted && <span className="shifted-badge" title={sc.resolutionReason}>SHIFT</span>}
                          {!shifted && sc.resolutionReason && <span className="info-badge" title={sc.resolutionReason}>i</span>}
                        </div>
                        <div className="course-detail">{sc.instructorName||`Egitmen ${sc.instructorId}`}</div>
                        <div className="course-detail">{sc.roomName||`Oda ${sc.roomId+1}`} ({sc.roomCapacity})</div>
                      </div>
                      <div className="course-meta">
                        <span className="enrollment-badge">{sc.enrollmentCount} kayit</span>
                        {isCrit && <span className="critical-badge">CP:{sc.criticalPathLength}</span>}
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

// ── Kart Grid ───────────────────────────────────────────────────────────────────
function CardGrid({ schedule }) {
  if (!schedule.length) return <div className="status-center"><p>Zamanlanmis ders yok.</p></div>;
  return (
    <div className="card-grid">
      {schedule.map((sc,i) => (
        <div key={i} className={`card-item ${deptClass(sc.departmentName)} ${sc.wasShifted?'card-item-shifted':''}`}>
          <div className="card-item-header">
            <span style={{fontWeight:700,fontSize:'0.9rem'}}>{sc.courseName||`Ders ${sc.courseId}`}</span>
            {sc.criticalPathLength>=3 && <span className="critical-badge">CP</span>}
          </div>
          <div className="card-item-body">
            <div><strong>{sc.dayName}</strong> {sc.hourLabel}</div>
            <div className="card-label">Departman: <strong>{sc.departmentName||'--'}</strong></div>
            <div className="card-label">Egitmen: {sc.instructorName||`Egitmen ${sc.instructorId}`}</div>
            <div className="card-label">Oda: {sc.roomName||`Oda ${sc.roomId+1}`} ({sc.roomCapacity} kisi)</div>
            <div className="card-label">Kayit: {sc.enrollmentCount} | Kritik Yol: {sc.criticalPathLength}</div>
            {sc.resolutionReason && <div className="card-reason">{sc.resolutionReason}</div>}
          </div>
        </div>
      ))}
    </div>
  );
}

export default App;