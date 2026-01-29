import React, { useState, useEffect } from "react"
import { useAuth0 } from "@auth0/auth0-react"
import { useTranslation } from "react-i18next"
import { Label } from "./label"
import { Input } from "./input"
import { Textarea } from "./textarea"
import { Button } from "./button"
import { Send, X } from "lucide-react"
import { api } from "../api/http"

function ContactUs({ isOpen, onClose }) {
  const { t } = useTranslation();
  const [subject, setSubject] = useState("")
  const [details, setDetails] = useState("")
  const [isSending, setIsSending] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)
  const [profileName, setProfileName] = useState("")
  const [loadingName, setLoadingName] = useState(false)

  const { isAuthenticated, user, getAccessTokenSilently, loginWithRedirect } = useAuth0()
  const isFormInvalid = subject.trim() === "" || details.trim() === "";

  // Redirect to login if modal opens and user is not authenticated
  useEffect(() => {
    if (isOpen && !isAuthenticated) {
      onClose();
      loginWithRedirect();
    }
  }, [isOpen, isAuthenticated, onClose, loginWithRedirect]);

  // Fetch profile name when modal opens
  useEffect(() => {
    if (!isOpen || !user) return;

    const fetchProfileName = async () => {
      setLoadingName(true);
      try {
        const token = await getAccessTokenSilently({
          authorizationParams: {
            audience: "https://vladtech/api",
          },
        });

        const userId = user?.sub || user?.id;
        const res = await api.get("/public/profile", {
          params: { userId },
          headers: { Authorization: `Bearer ${token}` },
        });

        // If response is a JSON object with name/nickname property
        if (typeof res?.data === "object") {
          setProfileName(res.data.name || res.data.nickname || user?.name || "");
        } else {
          // Fallback for string response
          const text = res?.data ?? "";
          const cleaned = typeof text === "string" ? text.replace(/^User Name:\s*/i, "") : "";
          setProfileName(cleaned || user?.name || "");
        }
      } catch (err) {
        console.error("Failed to fetch profile name:", err);
        setProfileName(user?.name || "");
      } finally {
        setLoadingName(false);
      }
    };

    fetchProfileName();
  }, [isOpen, user, getAccessTokenSilently]);

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

      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: "https://vladtech/api",
        },
      })

      // Use profileName from the database
      const payload = {
        subject,
        message: details,
        name: profileName || user?.name || user?.nickname || "",
        email: user?.email || "",
      }

      const response = await fetch("/api/contact", {
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
      setTimeout(() => {
        onClose()
        setSuccess(false)
      }, 2000)
    } catch (err) {
      console.error("Failed to send contact message", err)
      setError("Something went wrong while sending your message.")
    } finally {
      setIsSending(false)
    }
  }

  if (!isOpen) return null

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/0 backdrop-blur-0 animate-in fade-in duration-300"
      onClick={onClose}
      style={{
        animation: 'fadeInBackdrop 0.3s ease-out forwards'
      }}
    >
      <style>
        {`
          @keyframes fadeInBackdrop {
            from {
              background-color: rgba(0, 0, 0, 0);
              backdrop-filter: blur(0px);
            }
            to {
              background-color: rgba(0, 0, 0, 0.8);
              backdrop-filter: blur(8px);
            }
          }
          @keyframes modalSlideIn {
            from {
              opacity: 0;
              transform: scale(0.95) translateY(20px);
            }
            to {
              opacity: 1;
              transform: scale(1) translateY(0);
            }
          }
        `}
      </style>
      <div 
        className="relative w-full max-w-2xl mx-4 bg-gradient-to-b from-gray-900 to-black border border-white/10 rounded-3xl shadow-2xl"
        onClick={(e) => e.stopPropagation()}
        style={{
          animation: 'modalSlideIn 0.4s ease-out forwards',
          animationDelay: '0.1s',
          opacity: 0
        }}
      >
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-6 right-6 text-white/60 hover:text-white transition-colors"
        >
          <X className="h-6 w-6" />
        </button>

        <div className="p-10">
          <h1 className="text-4xl md:text-5xl text-white mb-2 tracking-tight">{t('contact.title')}</h1>
          <p className="text-gray-400 mb-8 tracking-wide">{t('contact.shareDetails')}</p>

          {!isAuthenticated ? (
            <div className="text-center py-12">
              <p className="text-white/80 mb-4">Please log in to send us a message</p>
              <Button
                onClick={onClose}
                className="bg-gradient-to-r from-yellow-400 to-yellow-500 hover:from-yellow-500 hover:to-yellow-600 text-black"
              >
                Close
              </Button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-3">
                <Label htmlFor="subject" className="text-white/60 tracking-wider text-xs uppercase">
                  {t('contact.subject')}
                </Label>
                <Input
                  id="subject"
                  type="text"
                  placeholder={t('contact.enterSubject')}
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                  required
                  className="bg-white/5 border-white/10 focus:border-yellow-400/50 text-white placeholder:text-white/30 backdrop-blur-xl h-12"
                />
              </div>

              <div className="space-y-3">
                <Label htmlFor="details" className="text-white/60 tracking-wider text-xs uppercase">
                  {t('contact.projectDetails')}
                </Label>
                <Textarea
                  id="details"
                  rows={4}
                  placeholder={t('contact.tellUs')}
                  value={details}
                  onChange={(e) => setDetails(e.target.value)}
                  required
                  className="min-h-[140px] bg-white/5 border-white/10 focus:border-yellow-400/50 text-white placeholder:text-white/30 backdrop-blur-xl resize-none"
                />
              </div>

              <Button
                type="submit"
                disabled={isSending || isFormInvalid}
                className="w-full bg-gradient-to-r from-yellow-400 to-yellow-500 hover:from-yellow-500 hover:to-yellow-600 text-black h-14 tracking-widest transition-all duration-300 shadow-lg shadow-yellow-400/20 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Send className="mr-3 h-5 w-5" />
                {isSending ? "SENDING..." : t('contact.sendMessage')}
              </Button>

              {success && (
                <div className="text-center p-4 bg-green-500/20 border border-green-500/30 rounded-lg">
                  <p className="text-green-400">Your message was sent successfully!</p>
                </div>
              )}
              {error && (
                <div className="text-center p-4 bg-red-500/20 border border-red-500/30 rounded-lg">
                  <p className="text-red-400">{error}</p>
                </div>
              )}
            </form>
          )}
        </div>
      </div>
    </div>
  )
}

export default ContactUs
