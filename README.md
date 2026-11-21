# 🌻🧩 Meu Girassol — Crachá Digital para Pessoas com Deficiências Ocultas

Aplicativo Android nativo desenvolvido para oferecer autonomia, segurança e acessibilidade a pessoas com deficiências não aparentes (como Autismo e outras condições ocultas). O app permite criar, personalizar e exibir um crachá de identificação digital com controle total sobre quais dados ficam privados no aparelho e quais são compartilhados via QR Code.

## 💡 A História por Trás do Projeto

O **Meu Girassol** nasceu de uma vivência real. Como usuária do cordão de girassol, passei por uma crise em que não conseguia falar — e, ao procurar ferramentas que me ajudassem a comunicar rapidamente o que eu precisava naquele momento, não encontrei nenhuma solução que atendesse a essa necessidade de forma prática e segura.

Percebendo que essa mesma barreira afeta milhares de outras pessoas com deficiências ocultas, uni minha experiência pessoal à minha atuação como **Engenheira de Software e Desenvolvedora** para projetar e construir do zero uma solução original, acessível, offline e centrada na privacidade do usuário.

## 🎯 Problema & Solução

Em situações de sobrecarga sensorial, crise ou dificuldade de comunicação verbal, pessoas com deficiências ocultas precisam transmitir informações essenciais (como alergias, necessidades de apoio, orientações de manejo e contatos de confiança) de forma imediata, sem abrir mão da sua privacidade no dia a dia.

O **Meu Girassol** resolve isso combinando **armazenamento 100% local**, **privacidade granular (*opt-in*)** e **geração de QR Code otimizado** para leitura instantânea.

## ✨ Funcionalidades Principais

* **Identificação Temática por Escolha Explícita:**
  * **🧩 Quebra-Cabeças (Autismo):** Exibe a simbologia do autismo com a mensagem *"Respeite meu tempo e minhas formas de comunicação"*.
  * **🌻 Cordão de Girassol (Outras Deficiências Ocultas):** Exibe a simbologia do girassol com a mensagem *"Meu cordão de girassol sinaliza uma deficiência não aparente. INCLUSÃO É RESPEITO!"*.
  * *Sem inferência automática:* O sistema nunca deduz a categoria pelo CID; a exibição na tela ou no QR depende exclusivamente da autorização do usuário.

* **Controle Granular de Privacidade (Toggles Individuais):**
  * Chaves individuais para ativar ou ocultar cada bloco de informação: **Nome ou Nome Social**, **CID**, **Alergias** (exibidas em tags de destaque), **Necessidades de Apoio**, **Orientações de Crise**, **Contatos de Confiança** e **Tipo Sanguíneo**.
  * Painel de resumo transparente mostrando exatamente quais campos estão **visíveis no QR Code** e quais permanecem **ocultos apenas no dispositivo**.

* **Exportação, Impressão e Pré-visualização:**
  * Geração e download direto da imagem do **QR Code** e do **Crachá completo** pronto para impressão.
  * Modo de pré-visualização (*"Ver como fica para quem escaneia"*) para validar os dados antes de compartilhar.

## 🏗️ Arquitetura e Decisões de Engenharia

* **Arquitetura Local-First & Privacy by Design:** Nenhum dado sensível é enviado a servidores externos. Todo o estado do perfil e as preferências de visibilidade residem localmente no dispositivo Android, garantindo funcionamento **100% offline** em momentos de crise.
* **Payload Compacto em Base64 (UTF-8):** Apenas os campos marcados explicitamente como públicos são serializados e codificados em Base64, preservando acentuação em português sem sobrecarga de processamento.
* **Otimização de Densidade do QR Code:** Limitação estratégica de caracteres nos campos de texto livre (como necessidades de apoio e orientações) para manter a matriz do QR Code limpa, garantindo leitura rápida até mesmo em câmeras de dispositivos mais antigos ou com brilho reduzido.
* **UI Acessível e de Baixa Carga Cognitiva:** Paleta de cores suaves de alto contraste, tipografia legível e componentes nativos desenhados para leitura rápida sob estresse.

## 🛠️ Stack Tecnológica

* **Plataforma:** Android Nativo
* **Linguagem:** Java (Android SDK)
* **Interface & Design System:** XML nativo com `drawables` vetoriais e *shapes* customizados (`badge_frame`, `photo_frame`, `badge_qr_frame`, faixas temáticas e tags de alerta)
* **Build & Automação:** Gradle / Gradle Wrapper

## 📂 Estrutura do Projeto

```text
meu-girassol-android/
├── app/src/main/
│   ├── java/com/meugirassol/app/
│   │   └── MainActivity.java          # Regras de negócio, toggles de privacidade, Base64 e geração do QR Code
│   ├── res/
│   │   ├── drawable/                  # Molduras, badges, faixas temáticas e componentes visuais
│   │   ├── layout/activity_main.xml   # Estrutura da interface e cartões de controle
│   │   └── values/                    # Paleta acessível, estilos e strings
│   └── AndroidManifest.xml
├── netlify-site/                      # Base estrutural da visualização web (planejada para a v2)
├── plan.md                            # Documentação de arquitetura e planejamento
└── TODO.md                            # Backlog técnico e evolução
```

## 📲 Instalação e Distribuição

O projeto é gratuito e independente, distribuído atualmente de forma direta via APK para facilitar o acesso imediato sem barreiras:

### Download e Instalação (Android)

Baixe o instalador oficial diretamente na aba de Releases do projeto:
* **[Baixar Meu Girassol v1.3.0 (APK)](https://github.com/jfermartins/MeuGirassol/releases/tag/MeuGirassol-v1.0.0-vers%C3%A3o-inicial)**

## 🗺️ Próximos Passos (Roadmap v2)

* **Visualização Web Pública (Netlify):** Ativar o redirecionamento do QR Code para uma página HTTPS leve e responsiva, permitindo que terceiros (socorristas, atendentes ou equipe de transporte) leiam o perfil autorizado diretamente no navegador, sem precisar instalar nenhum aplicativo.

## 👩‍💻 Engenharia e Desenvolvimento

Projeto idealizado a partir de uma necessidade real e conduzido de ponta a ponta por **Jane Fernanda Martins** — *Engenheira de Software & Desenvolvedora*.

* **Escopo conduzido:** Idealização do produto, levantamento de requisitos reais de acessibilidade, prototipação de UX/UI, arquitetura de privacidade *local-first*, desenvolvimento nativo em Java/XML, otimização de payload Base64/QR Code e bateria de testes funcionais e de leitura com usuários reais.
