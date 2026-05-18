import { motion } from 'framer-motion'
import { cn } from '@/lib/utils'

export function Card({ className, children, ...props }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25 }}
      className={cn('card-premium overflow-hidden', className)}
      {...props}
    >
      {children}
    </motion.div>
  )
}

export function CardHeader({ className, ...props }) {
  return <motion.div className={cn('flex flex-col gap-1.5 p-5', className)} {...props} />
}

export function CardTitle({ className, ...props }) {
  return <h3 className={cn('text-lg font-semibold tracking-tight', className)} {...props} />
}

export function CardDescription({ className, ...props }) {
  return <p className={cn('text-sm text-text-muted', className)} {...props} />
}

export function CardContent({ className, ...props }) {
  return <motion.div className={cn('p-5 pt-0', className)} {...props} />
}

export function CardFooter({ className, ...props }) {
  return <div className={cn('flex items-center p-5 pt-0', className)} {...props} />
}
