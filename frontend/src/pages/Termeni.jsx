import LegalPage from './legal/LegalPage'

export default function Termeni() {
  return (
    <LegalPage title="Termeni și Condiții">
      <p>Utilizarea platformei Rentix implică acceptarea acestor termeni. Rentix facilitează închirierea și vânzarea între utilizatori verificați.</p>
      <p>Proprietarii sunt responsabili pentru acuratețea anunțurilor. Chiriașii trebuie să respecte perioadele rezervate și să confirme primirea produselor conform fluxului escrow.</p>
      <p>Plățile sunt procesate prin Stripe. Rentix poate reține comisioane conform planului ales (Standard sau PRO).</p>
      <p>Ne rezervăm dreptul de a suspenda conturi care încalcă regulile comunității.</p>
    </LegalPage>
  )
}
