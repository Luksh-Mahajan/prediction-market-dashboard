import { useEffect, useState } from 'react'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'
import './App.css'

const API = '/api'
const COLORS = ['#4f46e5', '#e11d48', '#16a34a', '#d97706']
const REFRESH_MS = 60000

function App() {
  const [markets, setMarkets] = useState([])
  const [selected, setSelected] = useState(null)
  const [chartData, setChartData] = useState([])
  const [outcomes, setOutcomes] = useState([])
  const [lastUpdated, setLastUpdated] = useState(null)
  const [error, setError] = useState(null)

  // Load the tracked markets now, then again every minute
  useEffect(() => {
    function loadMarkets() {
      fetch(`${API}/markets/latest`)
        .then((res) => res.json())
        .then((rows) => {
          setMarkets(rows)
          // If the open market was dropped from the list, close its chart
          setSelected((current) =>
            current && rows.some((m) => m.marketId === current.marketId)
              ? current
              : null
          )
          setLastUpdated(new Date())
          setError(null)
        })
        .catch(() => setError('Cannot reach the server'))
    }

    loadMarkets()
    const timer = setInterval(loadMarkets, REFRESH_MS)
    return () => clearInterval(timer)
  }, [])

  // Load the open market's price history now, then again every minute
  useEffect(() => {
    if (!selected) return

    function loadHistory() {
      fetch(`${API}/markets/${selected.marketId}/prices`)
        .then((res) => res.json())
        .then((rows) => {
          const byTime = new Map()
          const names = new Set()
          rows.forEach((row) => {
            names.add(row.outcome)
            if (!byTime.has(row.capturedAt)) {
              byTime.set(row.capturedAt, { time: row.capturedAt })
            }
            byTime.get(row.capturedAt)[row.outcome] = row.price
          })
          setChartData([...byTime.values()])
          setOutcomes([...names])
        })
        .catch(() => setError('Cannot reach the server'))
    }

    loadHistory()
    const timer = setInterval(loadHistory, REFRESH_MS)
    return () => clearInterval(timer)
  }, [selected])

  return (
    <div className="app">
      <h1>Prediction Market Dashboard</h1>
      <p className="status">
        {error
          ? error
          : lastUpdated
            ? `Updated ${lastUpdated.toLocaleTimeString()} · refreshes every minute`
            : 'Loading...'}
      </p>
      <div className="layout">
        <ul className="market-list">
          {markets.map((m) => (
            <li
              key={m.marketId}
              className={selected?.marketId === m.marketId ? 'active' : ''}
              onClick={() => setSelected(m)}
            >
              <span className="rank">#{m.volumeRank}</span> {m.question}
            </li>
          ))}
        </ul>

        <div className="chart-panel">
          {!selected ? (
            <p>Pick a market on the left to see its price history.</p>
          ) : (
            <>
              <h2>{selected.question}</h2>
              <p className="status">
                Tracked since{' '}
                {new Date(selected.streakStartedAt).toLocaleString()}
              </p>
              <ResponsiveContainer width="100%" height={400}>
                <LineChart data={chartData}>
                  <XAxis
                    dataKey="time"
                    tickFormatter={(t) =>
                      new Date(t).toLocaleTimeString([], {
                        hour: '2-digit',
                        minute: '2-digit',
                      })
                    }
                  />
                  <YAxis
                    domain={[0, 1]}
                    tickFormatter={(v) => `${Math.round(v * 100)}%`}
                  />
                  <Tooltip
                    labelFormatter={(t) => new Date(t).toLocaleString()}
                    formatter={(v) => `${(v * 100).toFixed(1)}%`}
                  />
                  <Legend />
                  {outcomes.map((name, i) => (
                    <Line
                      key={name}
                      type="monotone"
                      dataKey={name}
                      stroke={COLORS[i % COLORS.length]}
                    />
                  ))}
                </LineChart>
              </ResponsiveContainer>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default App