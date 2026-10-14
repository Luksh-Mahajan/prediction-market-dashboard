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

const API = 'http://localhost:8080/api'
const COLORS = ['#4f46e5', '#e11d48', '#16a34a', '#d97706']

function App() {
  const [markets, setMarkets] = useState([])
  const [selected, setSelected] = useState(null)
  const [chartData, setChartData] = useState([])
  const [outcomes, setOutcomes] = useState([])

  // Load the list of markets once, when the page opens
  useEffect(() => {
    fetch(`${API}/markets/latest`)
      .then((res) => res.json())
      .then((rows) => {
        const byId = new Map()
        rows.forEach((row) => {
          if (!byId.has(row.marketId)) {
            byId.set(row.marketId, {
              marketId: row.marketId,
              question: row.question,
            })
          }
        })
        setMarkets([...byId.values()])
      })
  }, [])

  // Load the price history whenever a different market is clicked
  useEffect(() => {
    if (!selected) return
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
  }, [selected])

  return (
    <div className="app">
      <h1>Prediction Market Dashboard</h1>
      <div className="layout">
        <ul className="market-list">
          {markets.map((m) => (
            <li
              key={m.marketId}
              className={selected?.marketId === m.marketId ? 'active' : ''}
              onClick={() => setSelected(m)}
            >
              {m.question}
            </li>
          ))}
        </ul>

        <div className="chart-panel">
          {!selected ? (
            <p>Pick a market on the left to see its price history.</p>
          ) : (
            <>
              <h2>{selected.question}</h2>
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