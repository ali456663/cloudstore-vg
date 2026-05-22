import { useEffect, useState } from 'react'
import './App.css'

const PRODUCT_SERVICE_URL = 'http://localhost:8093'

function App() {
  const [products, setProducts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function loadProducts() {
      try {
        const response = await fetch(`${PRODUCT_SERVICE_URL}/api/products`)

        if (!response.ok) {
          throw new Error('Could not load products')
        }

        const data = await response.json()
        setProducts(data)
      } catch (err) {
        setError('Kunde inte hämta produkter. Kontrollera att product-service körs på port 8093.')
      } finally {
        setIsLoading(false)
      }
    }

    loadProducts()
  }, [])

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">CloudStore</p>
          <h1>Produkter</h1>
        </div>
        <div className="service-status">
          <span></span>
          product-service: 8093
        </div>
      </header>

      {isLoading && <p className="state-message">Hämtar produkter...</p>}
      {error && <p className="error-message">{error}</p>}

      {!isLoading && !error && (
        <section className="product-grid" aria-label="Produktlista">
          {products.map((product) => (
            <article className="product-card" key={product.id}>
              <div className="image-wrap">
                <img src={product.image} alt={product.title} />
              </div>
              <div className="product-info">
                <p className="category">{product.category}</p>
                <h2>{product.title}</h2>
                <div className="card-footer">
                  <strong>${product.price}</strong>
                  <button type="button">Lägg till</button>
                </div>
              </div>
            </article>
          ))}
        </section>
      )}
    </main>
  )
}

export default App
