import { useEffect, useState } from 'react'
import './App.css'

const PRODUCT_SERVICE_URL = 'http://localhost:8093'
const USER_ORDER_SERVICE_URL = 'http://localhost:8094'

const colorOptions = ['Black', 'White', 'Blue', 'Green']
const sizeOptions = ['S', 'M', 'L', 'XL']

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
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [selectedColor, setSelectedColor] = useState(colorOptions[0])
  const [selectedSize, setSelectedSize] = useState(sizeOptions[1])
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false)
  const [authMode, setAuthMode] = useState('register')
  const [registerForm, setRegisterForm] = useState(emptyRegisterForm)
  const [loginForm, setLoginForm] = useState(emptyLoginForm)
  const [authMessage, setAuthMessage] = useState('')
  const [authError, setAuthError] = useState('')
  const [token, setToken] = useState(() => localStorage.getItem('cloudstoreToken') || '')
  const [currentUser, setCurrentUser] = useState(null)
  const [orderMessage, setOrderMessage] = useState('')
  const [orderError, setOrderError] = useState('')
  const [isBuying, setIsBuying] = useState(false)

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
        setError('Kunde inte hamta produkter. Kontrollera att product-service kor pa port 8093.')
      } finally {
        setIsLoading(false)
      }
    }

    loadProducts()
  }, [])

  async function loadCurrentUser(authToken) {
    if (!authToken) {
      setCurrentUser(null)
      return null
    }

    try {
      const response = await fetch(`${USER_ORDER_SERVICE_URL}/api/auth/me`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      })

      if (!response.ok) {
        throw new Error('Could not load current user')
      }

      const data = await response.json()
      setCurrentUser(data)
      return data
    } catch (err) {
      return null
    }
  }

  useEffect(() => {
    loadCurrentUser(token)
  }, [token])

  function openProduct(product) {
    setSelectedProduct(product)
    setSelectedColor(colorOptions[0])
    setSelectedSize(sizeOptions[1])
    setIsCheckoutOpen(false)
    setAuthMode('register')
    setOrderMessage('')
    setOrderError('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function closeProduct() {
    setSelectedProduct(null)
    setIsCheckoutOpen(false)
    setAuthMode('register')
    setOrderMessage('')
    setOrderError('')
  }

  function openCheckout() {
    setIsCheckoutOpen(true)
    setAuthMode('register')
    setTimeout(() => {
      document.getElementById('checkout')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }, 0)
  }

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
      setAuthMessage(`Konto skapat for ${data.firstName}. Logga in for att slutfora kopet.`)
      setLoginForm({
        email: registerForm.email,
        password: '',
      })
      setRegisterForm(emptyRegisterForm)
      setAuthMode('login')
    } catch (err) {
      setAuthError('Registrering misslyckades. Testa annan e-post eller kontrollera falten.')
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
      const loginEmail = loginForm.email
      localStorage.setItem('cloudstoreToken', data.token)
      setToken(data.token)
      setCurrentUser({
        firstName: 'Inloggad',
        lastName: 'kund',
        email: loginEmail,
        phoneNumber: '',
      })
      loadCurrentUser(data.token)
      setLoginForm(emptyLoginForm)
      setAuthMessage('Du ar inloggad. Tryck pa Slutfor kop vid produkten.')
      setTimeout(() => {
        document.getElementById('checkout')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }, 0)
    } catch (err) {
      setAuthError('Inloggning misslyckades. Kontrollera e-post och losenord.')
    }
  }

  function logout() {
    localStorage.removeItem('cloudstoreToken')
    setToken('')
    setCurrentUser(null)
    setAuthMessage('Du ar utloggad.')
  }

  async function buySelectedProduct(authToken) {
    setOrderMessage('')
    setOrderError('')
    const storedToken = localStorage.getItem('cloudstoreToken')
    const activeToken = authToken || token || storedToken || ''

    if (!selectedProduct) {
      return
    }

    if (!activeToken) {
      setAuthMode('login')
      setAuthMessage('')
      setOrderError('Du ar inte inloggad just nu. Logga in igen och tryck sedan pa Slutfor kop.')
      return
    }

    setIsBuying(true)

    try {
      const response = await fetch(`${USER_ORDER_SERVICE_URL}/api/orders`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${activeToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          items: [
            {
              productId: selectedProduct.id,
              productTitle: selectedProduct.title,
              selectedColor,
              selectedSize,
              quantity: 1,
            },
          ],
        }),
      })

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          localStorage.removeItem('cloudstoreToken')
          setToken('')
          setCurrentUser(null)
          setAuthMode('login')
          throw new Error('login-required')
        }

        throw new Error('order-failed')
      }

      const data = await response.json()
      setOrderMessage(`Din produkt har registrerats. Bestallning #${data.id} ar skapad och saljaren far mejl.`)
    } catch (err) {
      if (err.message === 'login-required') {
        setOrderError('Din inloggning gick ut. Logga in igen och tryck sedan pa Slutfor kop.')
      } else {
        setOrderError('Kopet misslyckades. Kontrollera att user-order-service kor pa port 8094.')
      }
    } finally {
      setIsBuying(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Super Mix</p>
          <h1>Super Mix</h1>
        </div>
        {currentUser && (
          <div className="account-chip">
            <span>{currentUser.firstName} {currentUser.lastName}</span>
            <button type="button" onClick={logout}>Logga ut</button>
          </div>
        )}
      </header>

      {authMessage && <p className="state-message">{authMessage}</p>}
      {authError && <p className="error-message">{authError}</p>}
      {orderMessage && <p className="state-message">{orderMessage}</p>}
      {orderError && <p className="error-message">{orderError}</p>}

      {selectedProduct && (
        <section className="product-detail" aria-label="Produktdetaljer">
          <button className="text-button" type="button" onClick={closeProduct}>Tillbaka till produkter</button>
          <div className="detail-layout">
            <div className="detail-image">
              <img src={selectedProduct.image} alt={selectedProduct.title} />
            </div>
            <div className="detail-info">
              <p className="category">{selectedProduct.category}</p>
              <h2>{selectedProduct.title}</h2>
              <p className="description">{selectedProduct.description}</p>
              <strong className="detail-price">${selectedProduct.price}</strong>

              <div className="option-group">
                <label htmlFor="color">Farg</label>
                <select id="color" value={selectedColor} onChange={(event) => setSelectedColor(event.target.value)}>
                  {colorOptions.map((color) => (
                    <option key={color} value={color}>{color}</option>
                  ))}
                </select>
              </div>

              <div className="option-group">
                <label htmlFor="size">Storlek</label>
                <select id="size" value={selectedSize} onChange={(event) => setSelectedSize(event.target.value)}>
                  {sizeOptions.map((size) => (
                    <option key={size} value={size}>{size}</option>
                  ))}
                </select>
              </div>

              <button type="button" onClick={openCheckout}>Ga till kassa</button>
            </div>
          </div>
        </section>
      )}

      {selectedProduct && isCheckoutOpen && (
        <section className="checkout-panel" id="checkout" aria-label="Kassa">
          <div className="checkout-summary">
            <p className="eyebrow">Kassa</p>
            <h2>Slutfor kop</h2>
            <p>{selectedProduct.title}</p>
            <p>Farg: {selectedColor}</p>
            <p>Storlek: {selectedSize}</p>
            <strong>${selectedProduct.price}</strong>
            <p className="checkout-help">
              {currentUser
                ? 'Du ar inloggad. Tryck pa knappen nedan for att registrera produkten.'
                : 'Logga in eller registrera dig, tryck sedan pa Slutfor kop.'}
            </p>
            <button type="button" onClick={() => buySelectedProduct()} disabled={isBuying}>
              {isBuying ? 'Registrerar...' : 'Slutfor kop'}
            </button>
          </div>

          {currentUser ? (
            <div className="checkout-auth">
              <h2>Kund</h2>
              <p>{currentUser.firstName} {currentUser.lastName}</p>
              <p>{currentUser.email}</p>
              <p>{currentUser.phoneNumber}</p>
            </div>
          ) : (
            <div className="checkout-auth">
              {authMode === 'register' ? (
                <form className="auth-form checkout-form" onSubmit={register}>
                  <h2>Registrera kund</h2>
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
                    Losenord
                    <input name="password" type="password" value={registerForm.password} onChange={updateRegisterForm} required minLength={8} />
                  </label>
                  <button type="submit">Skapa konto</button>
                  <button className="secondary-button" type="button" onClick={() => setAuthMode('login')}>
                    Jag har redan konto - logga in
                  </button>
                </form>
              ) : (
                <form className="auth-form checkout-form" onSubmit={login}>
                  <h2>Logga in</h2>
                  <label>
                    Gmail / e-post
                    <input name="email" type="email" value={loginForm.email} onChange={updateLoginForm} required />
                  </label>
                  <label>
                    Losenord
                    <input name="password" type="password" value={loginForm.password} onChange={updateLoginForm} required />
                  </label>
                  <button type="submit">Logga in</button>
                  <button className="secondary-button" type="button" onClick={() => setAuthMode('register')}>
                    Ny kund - registrera dig
                  </button>
                </form>
              )}
            </div>
          )}
        </section>
      )}

      {isLoading && <p className="state-message">Hamtar produkter...</p>}
      {error && <p className="error-message">{error}</p>}

      {!isLoading && !error && !selectedProduct && (
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
                  <button type="button" onClick={() => openProduct(product)}>Visa detaljer</button>
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
