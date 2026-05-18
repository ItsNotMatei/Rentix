import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="mt-16 border-t border-border bg-white">
      <div className="container-rentix grid gap-8 py-12 md:grid-cols-4">
        <div>
          <p className="text-lg font-bold text-brand-700">Rentix</p>
          <p className="mt-2 text-sm text-text-muted">
            Platformă modernă de închiriere peer-to-peer. Sigur, rapid, family-friendly.
          </p>
        </div>
        <FooterCol title="Platformă" links={[
          ['Anunțuri', '/anunturi'],
          ['Cum funcționează', '/cum-functioneaza'],
          ['Calendar', '/calendar'],
        ]} />
        <FooterCol title="Cont" links={[
          ['Profil', '/profile'],
          ['Mesaje', '/chat'],
          ['Favorite', '/profile?tab=favorite'],
        ]} />
        <FooterCol title="Legal" links={[
          ['Termeni', '/termeni'],
          ['Confidențialitate', '/confidentialitate'],
        ]} />
      </div>
      <div className="border-t border-border py-4 text-center text-xs text-text-muted">
        © {new Date().getFullYear()} Rentix. Toate drepturile rezervate.
      </div>
    </footer>
  )
}

function FooterCol({ title, links }) {
  return (
    <div>
      <p className="mb-3 text-sm font-semibold">{title}</p>
      <ul className="space-y-2 text-sm text-text-muted">
        {links.map(([label, href]) => (
          <li key={label}>
            <Link to={href} className="hover:text-brand-700">{label}</Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
