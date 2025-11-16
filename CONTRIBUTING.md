# Como Contribuir para o NEOPARK

Ficamos felizes por seu interesse em contribuir com o NEOPARK! Este projeto é um esforço acadêmico da CESAR School, e toda ajuda é bem-vinda.

Para garantir um ambiente saudável e organizado, pedimos que siga estas diretrizes.

## 💬 Código de Conduta

Esperamos que todos os contribuidores sigam um código de conduta respeitoso. Seja gentil e construtivo nas discussões, issues e pull requests.

## 🚀 Como Começar: Montando seu Ambiente

Para contribuir, você primeiro precisa ter o projeto rodando localmente. Siga estes passos para baixar o software necessário e configurar seu ambiente.

### 1. Pré-requisitos (O que você precisa baixar)

Antes de começar, garanta que você tenha as seguintes ferramentas instaladas em sua máquina:

* **Git:** Essencial para clonar (baixar) o código e gerenciar suas alterações.
    * *Onde baixar:* [https://git-scm.com/downloads](https://git-scm.com/downloads)
* **Java Development Kit (JDK) 21:** O projeto usa Java 21.
    * *Onde baixar:* Recomendamos o [Eclipse Temurin (Adoptium)](https://adoptium.net/) ou [OpenJDK](https://jdk.java.net/21/).
* **Apache Maven:** Usado para compilar o projeto e gerenciar as dependências.
    * *Onde baixar:* [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
* **Uma IDE (Opcional, mas recomendado):** Facilita muito o desenvolvimento.
    * *Sugestões:* [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/), [VS Code com o Java Extension Pack](https://code.visualstudio.com/) ou [Eclipse](https://www.eclipse.org/downloads/).

### 2. Configuração do Ambiente (Montando o projeto)

1.  **Faça um Fork:** Clique no botão "Fork" no canto superior direito da página do repositório original do NEOPARK no GitHub. Isso criará uma cópia sua.

2.  **Clone o seu Fork:** Baixe o código do *seu* fork para sua máquina local usando o Git:
    ```bash
    # Substitua SEU-USUARIO pelo seu nome de usuário do GitHub
    git clone [https://github.com/SEU-USUARIO/neopark.git](https://github.com/SEU-USUARIO/neopark.git)
    cd neopark
    ```

3.  **Abra o Projeto na sua IDE:**
    * Abra sua IDE (IntelliJ, VS Code, etc.).
    * Use a opção "Open Project" ou "Import Project".
    * Selecione o diretório `neopark` que você acabou de clonar.
    * A IDE deve reconhecê-lo como um projeto Maven (por causa do arquivo `pom.xml`).

4.  **Sincronize as Dependências:**
    * Sua IDE provavelmente fará isso automaticamente. Ela vai ler o `pom.xml` e baixar todas as dependências necessárias (como Spring Boot, JPA, H2, etc.).
    * Se não for automático, procure por uma opção "Sync" ou "Reload" do Maven na sua IDE.

5.  **Execute o Projeto:**
    * Encontre a classe principal `EstacionamentoApplication.java`.
    * Clique com o botão direito e selecione "Run" ou "Debug".
    * O projeto deve iniciar. Ao rodar pela primeira vez, ele criará automaticamente o banco de dados H2 em um arquivo local no diretório `data/neoparkdb`.

Pronto! Agora seu ambiente está configurado para você começar a contribuir.

## 🛠️ Fluxo de Contribuição

1.  **Crie uma Nova Branch:**
    Sempre crie uma nova branch para sua feature ou correção de bug.
    ```bash
    git checkout -b feature/minha-nova-feature
    ```
    ou
    ```bash
    git checkout -b fix/correcao-de-bug
    ```

2.  **Faça suas Alterações:**
    Escreva seu código, mantendo o estilo e os padrões já utilizados no projeto. Se adicionar novas funcionalidades, por favor, inclua também testes (o projeto usa `spring-boot-starter-test`).

3.  **Faça o Commit:**
    Use mensagens de commit claras. Recomendamos seguir o padrão [Conventional Commits](https://www.conventionalcommits.org/):
    ```bash
    git commit -m "feat: Adiciona funcionalidade de..."
    ```

4.  **Envie suas Alterações:**
    Faça o push da sua branch para o seu fork:
    ```bash
    git push origin feature/minha-nova-feature
    ```

5.  **Abra um Pull Request (PR):**
    Vá até o repositório original do NEOPARK no GitHub e abra um Pull Request da sua branch para a branch `main` do projeto.

    * Descreva claramente o que foi feito no PR.
    * Se o PR resolver uma Issue aberta, mencione o número dela (ex: "Closes #123").

6.  **Revisão de Código:**
    Aguarde a revisão da equipe. Esteja aberto a feedbacks e a fazer os ajustes necessários.

## 🐛 Reportando Bugs

* Abra uma **Issue** no repositório.
* Descreva o problema detalhadamente.
* Inclua passos claros para reproduzir o bug.
* Informe o que você esperava que acontecesse e o que de fato aconteceu.

## ⭐ Sugerindo Melhorias

* Abra uma **Issue** com o rótulo (label) "enhancement" ou "sugestão".
* Explique sua ideia em detalhes e por que ela seria uma boa adição ao NEOPARK.

Obrigado por sua contribuição!

**Equipe NEOPARK**