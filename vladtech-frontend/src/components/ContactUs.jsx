import React, { useState } from "react"

function ContactUs({ onSubmit }) {
  const [subject, setSubject] = useState("")
  const [details, setDetails] = useState("")

  const handleSubmit = (e) => {
    e.preventDefault()

    const payload = {
      subject,
      projectDetails: details,
    }

    if (onSubmit) {
      onSubmit(payload)
    } else {
      // temporary placeholder
      console.log("Contact form submitted", payload)
    }

  }

  return (
    <section>
      <h1>CONTACT US</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label>
            <span>Subject</span>
            <input
              type="text"
              placeholder="Enter text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
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
            />
          </label>
        </div>

        <button type="submit">
          CONTACT US
        </button>
      </form>
    </section>
  )
}

export default ContactUs
