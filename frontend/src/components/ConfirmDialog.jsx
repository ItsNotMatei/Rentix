import * as Dialog from '@radix-ui/react-dialog'
import { Button } from '@/components/ui/button'

export default function ConfirmDialog({ open, onOpenChange, title, description, confirmLabel = 'Șterge', onConfirm, loading }) {
  return (
    <Dialog.Root open={open} onOpenChange={onOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-black/40 data-[state=open]:animate-in data-[state=closed]:animate-out" />
        <Dialog.Content className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-2xl border border-border bg-white p-6 shadow-xl">
          <Dialog.Title className="text-lg font-semibold text-slate-900">{title}</Dialog.Title>
          {description && (
            <Dialog.Description className="mt-2 text-sm text-text-muted">{description}</Dialog.Description>
          )}
          <div className="mt-6 flex justify-end gap-3">
            <Dialog.Close asChild>
              <Button type="button" variant="secondary">Anulează</Button>
            </Dialog.Close>
            <Button
              type="button"
              className="bg-red-600 hover:bg-red-700 text-white"
              disabled={loading}
              onClick={onConfirm}
            >
              {loading ? 'Se șterge...' : confirmLabel}
            </Button>
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
