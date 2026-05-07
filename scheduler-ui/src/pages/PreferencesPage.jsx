import { useState, useEffect } from 'react';

const API = 'http://localhost:8080/api';
const DAYS  = ['Pazartesi','Sali','Carsamba','Persembe','Cuma'];
const HOURS = ['08:00','09:00','10:00','11:00','12:00','13:00','14:00','15:00','16:00','17:00'];
const SCORE_LABELS = { 0: 'Yasakli', 1: 'Istenmiyor', 2: 'Notr', 3: 'Tercih' };
const SCORE_COLORS = { 0: '#ef4444', 1: '#f59e0b', 2: '#64748b', 3: '#10b981' };

export default function PreferencesPage() {
  const [instructors, setInstructors] = useState([]);
  const [preferences, setPreferences] = useState([]);
  const [selectedInstr, setSelectedInstr] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetch(`${API}/instructors`).then(r => r.json()).then(setInstructors).catch(() => {});
  }, []);

  useEffect(() => {
    if (selectedInstr) {
      fetch(`${API}/preferences/instructor/${selectedInstr}`)
        .then(r => r.json()).then(setPreferences).catch(() => {});
    } else {
      setPreferences([]);
    }
  }, [selectedInstr]);

  const getSlotPref = (day, hour) => {
    const slot = day * 10 + hour;
    return preferences.find(p => p.slot === slot);
  };

  const getSlotScore = (day, hour) => {
    const pref = getSlotPref(day, hour);
    return pref ? pref.preferenceScore : 2;
  };

  const cycleSlot = async (day, hour) => {
    if (!selectedInstr) return;
    setSaving(true);
    const slot = day * 10 + hour;
    const existing = getSlotPref(day, hour);
    const currentScore = existing ? existing.preferenceScore : 2;
    const nextScore = (currentScore + 1) % 4;
    const available = nextScore > 0;

    try {
      if (existing) {
        await fetch(`${API}/preferences/${existing.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ instructorId: +selectedInstr, slot, available, preferenceScore: nextScore })
        });
      } else {
        await fetch(`${API}/preferences`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ instructorId: +selectedInstr, slot, available, preferenceScore: nextScore })
        });
      }
      const res = await fetch(`${API}/preferences/instructor/${selectedInstr}`);
      setPreferences(await res.json());
    } catch (e) { console.error(e); }
    finally { setSaving(false); }
  };

  const instrName = instructors.find(i => String(i.id) === String(selectedInstr));

  return (
    <div className="crud-page">
      <div className="crud-header">
        <h2>Egitmen Tercih Yonetimi</h2>
        <span className="crud-count">{preferences.length} tercih kaydi</span>
      </div>

      <div className="pref-controls">
        <select className="pref-select" value={selectedInstr} onChange={e => setSelectedInstr(e.target.value)}>
          <option value="">-- Egitmen Secin --</option>
          {instructors.map(i => (
            <option key={i.id} value={i.id}>{i.title} {i.name}</option>
          ))}
        </select>
        {instrName && <span className="pref-instr-name">{instrName.title} {instrName.name}</span>}
      </div>

      <div className="pref-legend">
        {Object.entries(SCORE_LABELS).map(([score, label]) => (
          <div key={score} className="pref-legend-item">
            <div className="pref-legend-dot" style={{ background: SCORE_COLORS[score] }}/>
            <span>{label} ({score})</span>
          </div>
        ))}
        <span className="pref-hint">Hucrelere tiklayarak tercihi degistirin</span>
      </div>

      {selectedInstr ? (
        <div className="pref-grid-wrap">
          <table className="pref-grid">
            <thead>
              <tr>
                <th>Saat</th>
                {DAYS.map(d => <th key={d}>{d}</th>)}
              </tr>
            </thead>
            <tbody>
              {HOURS.map((hour, hIdx) => (
                <tr key={hour}>
                  <td className="pref-time">{hour}</td>
                  {DAYS.map((_, dIdx) => {
                    const score = getSlotScore(dIdx, hIdx);
                    return (
                      <td
                        key={dIdx}
                        className={`pref-cell pref-score-${score}`}
                        onClick={() => !saving && cycleSlot(dIdx, hIdx)}
                        title={`${DAYS[dIdx]} ${hour} - ${SCORE_LABELS[score]} (${score})`}
                      >
                        {SCORE_LABELS[score]}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="pref-empty">
          Tercih tablosunu goruntulemek icin bir egitmen secin.
        </div>
      )}
    </div>
  );
}
