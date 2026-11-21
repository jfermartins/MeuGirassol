# Meu Girassol — versão Android offline

## Escopo atual

Aplicativo Android nativo em Java e XML, sem servidor, hospedagem, API ou permissão de internet. Preservar o ícone launcher baseado no logotipo de girassol fornecido, os temas escolhidos manualmente e a geração do crachá imprimível.

## Comportamento

A pessoa edita seus dados localmente e escolhe os campos que podem aparecer no crachá/QR. Pode escolher Autismo (quebra-cabeça) ou Outra deficiência oculta (girassol); a categoria nunca é inferida pelo CID e só é adicionada ao payload se ela autorizar.

O QR offline contém um payload JSON serializado e codificado como Base64 URL-safe dentro do deep link `meugirassol://perfil?d=...`. Base64 não é criptografia. O leitor precisa ter o app instalado para o Android abrir o deep link e mostrar o perfil. O QR é uma cópia no momento da criação: alterações exigem gerar/distribuir um novo código. A foto permanece apenas no dispositivo.

## Arquitetura e componentes

- `app/src/main/...`: app Android Java/XML, armazenamento local, deep link, renderização do crachá e QR local.
- `app/src/main/res/mipmap-*` e `mipmap-anydpi-v26`: ícones PNG do launcher por densidade e ícone adaptável; girassol visível conforme logo fornecido.
- `app/src/main/res/drawable-nodpi/ic_launcher_foreground.png`: arte usada no ícone adaptável.
- `README.md`: instruções Android Studio, privacidade e limitação do QR offline.

## Privacidade e limites

Não há rede nem perfil público hospedado. Os dados incluídos no QR podem ser lidos por qualquer pessoa que tenha uma cópia; não armazenar informações que não queira revelar. Apagar dados locais não apaga QRs já compartilhados. Perfis eventualmente criados por uma versão online anterior não são consultados nem removidos por esta versão offline.
