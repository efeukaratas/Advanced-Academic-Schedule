import { useState, useEffect } from 'react';

function App() {
  const [schedule, setSchedule] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('http://localhost:8080/api/schedule/generate')
      .then(res => res.json())
      .then(data => {
        setSchedule(data);
        setLoading(false);
      })
      .catch(err => {
        console.error("Hata:", err);
        setLoading(false);
      });
  }, []);

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1 style={styles.title}> Academic Scheduler <span style={styles.version}>v1.0</span></h1>
        <p style={styles.subtitle}>O(1) Matrix-Based Optimization Results</p>
      </header>

      {loading ? (
        <p style={styles.status}>Yükleniyor...</p>
      ) : schedule.length > 0 ? (
        <div style={styles.gridContainer}>
          {schedule.map((item, idx) => (
            <div key={idx} style={styles.card}>
              <div style={styles.cardHeader}>Ders ID: {item.courseId}</div>
              <div style={styles.cardBody}>
                <p> Hoca: {item.instructorId}</p>
                <p> Sınıf: {item.roomId}</p>
                <p> Slot: {item.timeSlotId}</p>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p style={styles.status}>Veri bulunamadı. Backend'i kontrol et!</p>
      )}
    </div>
  );
}

const styles = {
  container: { backgroundColor: '#0f172a', color: '#f8fafc', minHeight: '100vh', padding: '40px', fontFamily: '"Inter", sans-serif' },
  header: { borderBottom: '1px solid #1e293b', marginBottom: '30px', paddingBottom: '20px' },
  title: { fontSize: '2.5rem', fontWeight: '800', margin: '0' },
  version: { fontSize: '1rem', color: '#38bdf8', verticalAlign: 'middle' },
  subtitle: { color: '#94a3b8', marginTop: '5px' },
  gridContainer: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '20px' },
  card: { backgroundColor: '#1e293b', borderRadius: '12px', overflow: 'hidden', border: '1px solid #334155', transition: 'transform 0.2s' },
  cardHeader: { backgroundColor: '#334155', padding: '10px 15px', fontWeight: 'bold', fontSize: '1.1rem' },
  cardBody: { padding: '15px', lineHeight: '1.6' },
  status: { fontSize: '1.2rem', color: '#fb7185', textAlign: 'center', marginTop: '50px' }
};

export default App;