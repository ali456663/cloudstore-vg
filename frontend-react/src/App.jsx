import { useEffect, useState } from 'react'
import './App.css'

const PRODUCT_SERVICE_URL = 'http://localhost:8093'
const USER_ORDER_SERVICE_URL = 'http://localhost:8094'

const emptyRegisterForm = {
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  password: '',
}

const emptyLoginForm = {
  email: '',
  password: '',
}

function App() {
  const [products, setProducts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [registerForm, setRegisterForm] = useState(emptyRegisterForm)
  const [loginForm, setLoginForm] = useState(emptyLoginForm)
  const [authMessage, setAuthMessage] = useState('')
  const [authError, setAuthError] = useState('')
  const [token, setToken] = useState(() => localStorage.getItem('cloudstoreToken') || '')
  const [currentUser, setCurrentUser] = useState(null)
  const [orderMessage, setOrderMessage] = useState('')
  const [orderError, setOrderError] = useState('')
  const [buyingProductId, setBuyingProductId] = useState(null)

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
        setError('Kunde inte hämta produkter. Kontrollera att product-service kör på port 8093.')
      } finally {
        setIsLoading(false)
      }
    }

    loadProducts()
  }, [])

  useEffect(() => {
    async function loadCurrentUser() {
      if (!token) {
        setCurrentUser(null)
        return
      }

      try {
        const response = await fetch(`${USER_ORDER_SERVICE_URL}/api/auth/me`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })

        if (!response.ok) {
          throw new Error('Could not load current user')
        }

        const data = await response.json()
        setCurrentUser(data)
      } catch (err) {
        localStorage.removeItem('cloudstoreToken')
        setToken('')
        setCurrentUser(null)
      }
    }

    loadCurrentUser()
  }, [token])

  function updateRegisterForm(event) {
    setRegisterForm({
      ...registerForm,
      [event.target.name]: event.target.value,
    })
  }

  function updateLoginForm(event) {
    setLoginForm({
      ...loginForm,
      [event.target.name]: event.target.value,
    })
  }

  async function register(event) {
    event.preventDefault()
    setAuthMessage('')
    setAuthError('')

    try {
      const response = await fetch(`${USER_ORDER_SERVICE_URL}/api/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(registerForm),
      })

      if (!response.ok) {
        throw new Error('Could not register user')
      }

      const data = await response.json()
      setAuthMessage(`Konto skapat for ${data.firstName}. Du kan logga in nu.`)
      setRegisterForm(emptyRegisterForm)
    } catch (err) {
      setAuthError('Registrering misslyckades. Testa annan e-post eller kontrollera fälten.')
    }
  }

  async function login(event) {
    event.preventDefault()
    setAuthMessage('')
    setAuthError('')

    try {
      const response = await fetch(`${USER_ORDER_SERVICE_URL}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginForm),
      })

      if (!response.ok) {
        throw new Error('Could not log in')
      }

      const data = await response.json()
      localStorage.setItem('cloudstoreToken', data.token)
      setToken(data.token)
      setLoginForm(emptyLoginForm)
      setAuthMessage('Du är inloggad.')
    } catch (err) {
      setAuthError('Inloggning misslyckades. Kontrollera e-post och lösenord.')
    }
  }

  function logout() {
    localStorage.removeItem('cloudstoreToken')
    setToken('')
    setCurrentUser(null)
    setAuthMessage('Du är utloggad.')
  }

  async function buyProduct(product) {
    setOrderMessage('')
    setOrderError('')

    if (!token || !currentUser) {
      setOrderError('Du behöver registrera dig eller logga in innan du kan köpa produkten.')
      return
    }

    setBuyingProductId(product.id)

    try {
      const response = await fetch(`${USER_ORDER_SERVICE_URL}/api/orders`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          items: [
            {
              productId: product.id,
              quantity: 1,
            },
          ],
        }),
      })

      if (!response.ok) {
        throw new Error('Could not create order')
      }

      const data = await response.json()
      setOrderMessage(`Beställning #${data.id} skapad. Ägaren får mejl via Google Script.`)
    } catch (err) {
      setOrderError('Köpet misslyckades. Kontrollera att user-order-service kör på port 8094.')
    } finally {
      setBuyingProductId(null)
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">CloudStore</p>
          <h1>Produkter</h1>
        </div>
        <div className="service-list">
          <div className="service-status">
            <span></span>
            product-service: 8093
          </div>
          <div className="service-status">
            <span></span>
            user-order-service: 8094
          </div>
        </div>
      </header>

      <section className="auth-panel" aria-label="Autentisering">
        <div className="auth-status">
          <p className="eyebrow">Konto</p>
          {currentUser ? (
            <>
              <h2>Inloggad som {currentUser.firstName} {currentUser.lastName}</h2>
              <p>{currentUser.email}</p>
              <p>{currentUser.phoneNumber}</p>
              <button type="button" onClick={logout}>Logga ut</button>
            </>
          ) : (
            <>
              <h2>Inte inloggad</h2>
              <p>Registrera dig en gång. Nästa gång loggar du in med Gmail och lösenord.</p>
            </>
          )}
        </div>

        <form className="auth-form" onSubmit={register}>
          <h2>Registrera</h2>
          <label>
            Namn
            <input name="firstName" value={registerForm.firstName} onChange={updateRegisterForm} required minLength={2} />
          </label>
          <label>
            Efternamn
            <input name="lastName" value={registerForm.lastName} onChange={updateRegisterForm} required minLength={2} />
          </label>
          <label>
            Gmail / e-post
            <input name="email" type="email" value={registerForm.email} onChange={updateRegisterForm} required />
          </label>
          <label>
            Telefonnummer
            <input name="phoneNumber" type="tel" value={registerForm.phoneNumber} onChange={updateRegisterForm} required minLength={7} />
          </label>
          <label>
            Lösenord
            <input name="password" type="password" value={registerForm.password} onChange={updateRegisterForm} required minLength={8} />
          </label>
          <button type="submit">Skapa konto</button>
        </form>

        <form className="auth-form" onSubmit={login}>
          <h2>Logga in</h2>
          <label>
            Gmail / e-post
            <input name="email" type="email" value={loginForm.email} onChange={updateLoginForm} required />
          </label>
          <label>
            Lösenord
            <input name="password" type="password" value={loginForm.password} onChange={updateLoginForm} required />
          </label>
          <button type="submit">Logga in</button>
        </form>
      </section>

      {authMessage && <p className="state-message">{authMessage}</p>}
      {authError && <p className="error-message">{authError}</p>}
      {orderMessage && <p className="state-message">{orderMessage}</p>}
      {orderError && <p className="error-message">{orderError}</p>}

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
                  <button type="button" onClick={() => buyProduct(product)} disabled={buyingProductId === product.id}>
                    {buyingProductId === product.id ? 'Köper...' : 'Köp'}
                  </button>
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
