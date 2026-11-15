# ADS-PI-3-FRONT
Neste repositório está localizado o FRONT-END do nosso projeto integrador da FATEC que consiste em um sistema de controle de cadastros, locações e caixa de uma loja de locações de roupas.

## Como rodar

Requisitos: JDK 26 ou mais novo e Maven.

```
mvn javafx:run
```

Por padrão a aplicação abre no modo de teste (`servico.modo=mock`), com dados falsos em memória. Login de teste: `admin@loja.com` / `admin123`.

## Ligar à API (dados gravados no banco)

Crie o arquivo `aplicacao.properties` dentro da pasta `.trajeloc` da sua pasta pessoal (no Windows, `C:\Users\<seu usuário>\.trajeloc\`; no Mac e no Linux, `~/.trajeloc/`):

```
servico.modo=api
api.url=http://<endereço do computador servidor>:3000
api.timeout.segundos=10
```

- Se a API estiver rodando no seu próprio computador, use `http://localhost:3000`.
- Se estiver em outro computador da mesma rede Wi-Fi, use o IP dele, por exemplo `http://192.168.0.46:3000`. O IP muda de uma rede para outra; confira no computador servidor antes de usar.

Depois é só rodar `mvn javafx:run`. O login passa a ser o dos usuários do banco (perfis ADMINISTRADOR e FUNCIONARIO). Para voltar aos dados de teste, apague o arquivo ou troque para `servico.modo=mock`.

O que já usa a API: login e tela de clientes. As demais telas ainda estão em construção.
