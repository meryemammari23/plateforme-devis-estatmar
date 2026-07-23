import { useState } from 'react'
import StatutBadge from './StatutBadge.jsx'
import { demandeApi, devisApi, downloadBlob } from '../api/endpoints.js'

const money = (v) =>
  new Intl.NumberFormat('fr-MA', { style: 'currency', currency: 'MAD' }).format(v || 0)

const dateFmt = (v) => (v ? new Date(v).toLocaleString('fr-FR', { dateStyle: 'medium', timeStyle: 'short' }) : '—')

export default function DemandeDetailModal({ demande, onClose, onChanged, footerExtra, canRefuse = true, refuseLabel = 'Refuser la demande' }) {
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState('')

  if (!demande) return null

  const handleDownloadPiece = async (pj) => {
    try {
      const { data } = await demandeApi.telechargerPieceJointe(pj.id)
      downloadBlob(data, pj.nomOriginal)
    } catch {
      setErr("Impossible de télécharger cette pièce jointe.")
    }
  }

  const handleDownloadDevis = async () => {
    try {
      const { data } = await devisApi.telechargerPdf(demande.devisId)
      downloadBlob(data, `devis-${demande.reference}.pdf`)
    } catch {
      setErr('Le PDF du devis est introuvable pour le moment.')
    }
  }

  const handleRefuser = async () => {
    setBusy(true)
    setErr('')
    try {
      await demandeApi.refuser(demande.id)
      onChanged?.()
      onClose()
    } catch (e) {
      setErr(e.response?.data?.message || 'Action impossible.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 560 }}>
        <div className="flex justify-between items-center" style={{ marginBottom: 16 }}>
          <div>
            <div className="eyebrow">Demande {demande.reference}</div>
            <h3 style={{ margin: 0 }}>Détail de la demande</h3>
          </div>
          <StatutBadge statut={demande.statut} />
        </div>

        {err && <div className="form-alert error">{err}</div>}

        <div className="field">
          <label>Description du projet</label>
          <p className="text-muted" style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{demande.description}</p>
        </div>

        <div className="flex gap-16" style={{ marginBottom: 14 }}>
          <div>
            <div className="text-sm text-muted">Budget proposé</div>
            <strong>{money(demande.budget)}</strong>
          </div>
          <div>
            <div className="text-sm text-muted">Client</div>
            <strong>{demande.clientNomComplet || '—'}</strong>
          </div>
          <div>
            <div className="text-sm text-muted">Employé attribué</div>
            <strong>{demande.employeNomComplet || 'Non attribué'}</strong>
          </div>
        </div>

        <div className="text-sm text-muted" style={{ marginBottom: 14 }}>
          Créée le {dateFmt(demande.dateCreation)} · Mise à jour le {dateFmt(demande.dateMaj)}
        </div>

        {demande.piecesJointes?.length > 0 && (
          <div className="field">
            <label>Pièces jointes</label>
            {demande.piecesJointes.map((pj) => (
              <div key={pj.id} className="flex justify-between items-center" style={{ padding: '8px 0', borderBottom: '1px solid var(--border-soft)' }}>
                <span className="text-sm">{pj.nomOriginal}</span>
                <button className="btn btn-ghost btn-sm" onClick={() => handleDownloadPiece(pj)}>Télécharger</button>
              </div>
            ))}
          </div>
        )}

        <div className="flex gap-8 mt-24" style={{ flexWrap: 'wrap' }}>
          {demande.devisId && (
            <button className="btn btn-primary btn-sm" onClick={handleDownloadDevis}>
              Télécharger le devis PDF
            </button>
          )}
          {canRefuse && (demande.statut === 'EN_ATTENTE' || demande.statut === 'EN_COURS') && (
            <button className="btn btn-danger btn-sm" onClick={handleRefuser} disabled={busy}>
              {busy ? 'Annulation en cours…' : refuseLabel}
            </button>
          )}
          {footerExtra}
          <button className="btn btn-outline btn-sm" onClick={onClose}>Fermer</button>
        </div>
      </div>
    </div>
  )
}