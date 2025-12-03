import React, { useState } from "react"
import { useAuth0 } from "@auth0/auth0-react"

function ContactUs() {
  const [subject, setSubject] = useState("")
  const [details, setDetails] = useState("")
  const [isSending, setIsSending] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)

  const { isAuthenticated, user, getAccessTokenSilently } = useAuth0()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setSuccess(false)

    if (!isAuthenticated) {
      setError("You must be logged in to contact us.")
      return
    }

    try {
      setIsSending(true)

      // Get JWT from Auth0 for the backend
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: "https://vladtech/api",
        },
      })

      // Backend DTO fields:
      // email, name, subject, message
      const payload = {
        subject,
        message: details,
        name: user?.name || user?.nickname || "",
        email: user?.email || "",
      }

      const response = await fetch("http://localhost:8080/api/contact", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      })

      if (!response.ok) {
        throw new Error(`Backend returned status ${response.status}`)
      }

      setSuccess(true)
      setSubject("")
      setDetails("")
    } catch (err) {
      console.error("Failed to send contact message", err)
      setError("Something went wrong while sending your message.")
    } finally {
      setIsSending(false)
    }
  }

  return (
    <section>
      <h1>CONTACT US</h1>

      {!isAuthenticated && (
        <p>You must be logged in to use this form.</p>
      )}

      <form onSubmit={handleSubmit}>
        <div>
          <label>
            <span>Subject</span>
            <input
              type="text"
              placeholder="Enter text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              required
            />
          </label>
        </div>

        <div>
          <label>
            <span>Project Details</span>
            <textarea
              rows={4}
              placeholder="Enter text"
              value={details}
              onChange={(e) => setDetails(e.target.value)}
              required
            />
          </label>
        </div>

        <button type="submit" disabled={!isAuthenticated || isSending}>
          {isSending ? "Sending..." : "CONTACT US"}
        </button>
      </form>

      {success && <p>Your message was sent successfully.</p>}
      {error && <p>{error}</p>}
    </section>
  )
}

export default ContactUs
