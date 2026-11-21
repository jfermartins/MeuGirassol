# Meu Girassol — Android nativo (Java + XML)

Aplicativo Android offline, desenvolvido em Java e XML para abrir no Android Studio. Permite editar o perfil no próprio aparelho, montar um crachá com foto e QR, e escolher entre o tema Autismo/quebra-cabeça ou outra deficiência oculta/girassol. O logotipo de girassol enviado foi integrado ao ícone do aplicativo, com recursos para diferentes densidades e ícone adaptável. **Esta versão não usa serviço online, servidor ou permissão de internet.**

## Abrir e executar

1. Instale Android Studio, Android SDK (API 35 ou superior recomendado) e JDK 17.
2. Extraia o ZIP do projeto.
3. No Android Studio, escolha **Open** e selecione a pasta `meu-girassol-android`, onde está `settings.gradle`.
4. Aguarde a sincronização do Gradle e a instalação dos componentes Android solicitados.
5. Escolha um emulador ou aparelho e pressione **Run**.

Para gerar um APK de depuração: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. O arquivo normalmente fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Como funciona o QR offline

O QR guarda, dentro do próprio código, os campos marcados para compartilhamento. O conteúdo é codificado para caber no QR, **mas não é criptografado**: qualquer pessoa que escanear o crachá pode ler essas informações. A foto não é incluída no QR; ela fica no aparelho e é impressa na imagem do crachá. O conteúdo do QR tem um limite de tamanho; se os textos forem longos demais, o app solicitará que reduza os dados.

Para abrir e exibir os dados do QR, o telefone que escanear precisa ter o Meu Girassol instalado. Ao ler o código com a câmera, o Android deve oferecer a abertura do link `meugirassol://perfil` no app. Sem o app instalado, esse link offline não abrirá uma página web — não há servidor ou site nesta versão.

Os dados do QR são uma cópia do perfil feita no momento em que o código foi criado. Se a pessoa editar o perfil, precisa gerar e imprimir/compartilhar um novo QR; códigos antigos que já foram distribuídos continuam mostrando os dados anteriores.

## Funcionalidades

- Perfil editável localmente: nome/nome social, CID, alergias, informações relevantes, necessidades de apoio, contato de confiança, orientações e campos personalizados.
- Ícone Meu Girassol na tela inicial do aparelho, baseado no logotipo fornecido; versão Android 1.3.0 (`versionCode 4`).
- Tema escolhido pela pessoa: Autismo (quebra-cabeça) ou Outra deficiência oculta (girassol). O app não tenta deduzir o tema pelo CID.
- Seleção individual das informações incluídas no crachá e no QR. Nome começa selecionado; CID e alergias começam privados. A categoria só entra no QR se a pessoa marcar a opção correspondente.
- Foto local escolhida pelo seletor de arquivos Android.
- QR Code criado sem conexão de internet nem armazenamento online.
- Prévia do crachá com foto, dados selecionados, tema escolhido e QR; opção para salvar a imagem PNG e imprimir.
- Apagar perfil, foto e QR armazenados neste aparelho.

## Privacidade e observações

Revise os campos marcados antes de gerar o QR. Evite incluir dados que não queira revelar a qualquer pessoa que escanear uma cópia do código. O QR não tem senha nem criptografia. Apagar os dados deste aparelho não apaga códigos anteriormente salvos, impressos ou enviados.

Se você já criou um perfil usando uma versão anterior com hospedagem online, esta versão offline não acessa nem remove aquele perfil remoto. Perfis online antigos, se houver, precisam ser tratados separadamente no serviço onde foram criados.

O crachá é uma ferramenta de comunicação e identificação; ele não substitui documentos, laudos ou atendimento de emergência.
# MeuGirassol
