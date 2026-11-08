# Backend de IA do SalCity — Gemini Flash-Lite

Este serviço conecta o app Android ao Gemini pelo servidor. O modelo padrão é `gemini-3.5-flash-lite`, opção atual recomendada pela documentação para novos projetos de baixo custo. **Não use Gemini 2.5 Flash em um projeto novo:** a documentação oficial atualmente informa que está depreciado/não selecionável no AI Studio. Cotas e disponibilidade gratuita podem mudar.

Referências oficiais: [modelo Gemini 3.5 Flash-Lite](https://ai.google.dev/gemini-api/docs/models/gemini-3.5-flash-lite), [preços e plano gratuito](https://aistudio.google.com/docs/pricing), [segurança de chaves](https://ai.google.dev/gemini-api/docs/api-key) e [respostas JSON estruturadas](https://ai.google.dev/gemini-api/docs/structured-output).

## Pré-requisitos e configuração

Requer Node.js 20 ou mais recente. Use `.env.example` apenas como referência das variáveis; o servidor não carrega um arquivo `.env` automaticamente e não precisa de dependências externas. No Windows, entre na pasta `backend`, configure as variáveis temporariamente na sessão do PowerShell e inicie o processo:

```powershell
$env:GEMINI_API_KEY = "SUA_CHAVE_GEMINI"
$env:EVENT_SOURCE_URLS = "https://dominio-oficial.example/agenda;https://outro-dominio.example/eventos"
Set-Location backend
npm start
```

Substitua os domínios de exemplo pelas fontes aprovadas pela equipe. Se nenhuma fonte HTTPS estiver configurada, `/events/search` responde `503` em vez de inventar eventos. O coletor só tenta as URLs configuradas no servidor, limita o tamanho do conteúdo, confere redirects e pede ao modelo que extraia apenas fatos encontrados na página. Sites que dependem de JavaScript, bloqueiam bots ou não publicam os dados em HTML estático podem não ser lidos; nesse caso, integre feeds/APIs oficiais desses sites. Antes de coletar páginas, verifique os termos de uso e as regras de acesso dos sites.

A chave do Gemini é lida pelo backend em `GEMINI_API_KEY` e enviada ao serviço de IA pelo cabeçalho `x-goog-api-key`. Não configure essa chave no Android, Gradle, Git ou no `.env.example`. Em produção, use HTTPS, gerenciador de segredos, limites de uso e autenticação para proteger o backend. A demonstração tem limitação simples por IP; ela não substitui rate-limit distribuído nem autenticação.

## Endpoints

`GET /api/health` informa disponibilidade, modelo e número de fontes configuradas; não testa a chave junto ao Google.

`POST /api/events/search` recebe `city`, `start_date`, `end_date`, `themes`. As datas são `YYYY-MM-DD`. Retorna `{ "events": [...] }`, com `source_url` que deve corresponder a uma página configurada no backend.

`POST /api/itineraries/generate` recebe `city`, `start_date`, `end_date`, `themes`, `transport`, `budget`, `include_restaurants`, `selected_events`. Retorna `{ "itinerary": { "title", "summary", "days": [...] } }`.

Teste a saúde e a geração de roteiro via PowerShell/curl:

```powershell
curl.exe http://localhost:8080/api/health
curl.exe -X POST http://localhost:8080/api/itineraries/generate `
  -H "Content-Type: application/json" `
  -d '{"city":"Salvador","start_date":"2026-12-20","end_date":"2026-12-21","themes":["Cultura"],"transport":"A pé","budget":"R$ 50 a 200","include_restaurants":false,"selected_events":[{"name":"Evento de teste","start_datetime":"2026-12-20T19:00:00-03:00","venue":"Centro","source_url":"https://dominio-oficial.example/evento"}]}'
```

Teste a busca após configurar as fontes:

```powershell
curl.exe -X POST http://localhost:8080/api/events/search `
  -H "Content-Type: application/json" `
  -d '{"city":"Salvador","start_date":"2026-12-20","end_date":"2026-12-23","themes":["Cultura","Eventos"]}'
```

## Apontar o app Android ao backend local

Para o emulador Android padrão, use `10.0.2.2` para acessar o computador hospedeiro. O manifesto de debug permite HTTP local apenas em builds de desenvolvimento; a versão release permanece com a política normal de tráfego seguro. A URL pode ser definida na compilação via propriedade Gradle, sem chave secreta:

```powershell
.\gradlew.bat :app:assembleDebug -PSALCITY_AI_BASE_URL=http://10.0.2.2:8080/api
```

Em aparelho físico, configure `SALCITY_AI_BASE_URL` com o endereço LAN do computador e permita o acesso pela rede local. Em produção, use uma URL HTTPS. A tela **Eventos** na Home abre o novo fluxo Android de IA: o turista seleciona datas e interesses, consulta eventos, marca as opções desejadas e pede a geração do roteiro. Os filtros tradicionais da Home permanecem independentes.

## Dados e nível gratuito

A modalidade gratuita do Google AI Studio pode ter cotas diferentes por modelo, projeto e região, e o Google pode alterar ou restringir o acesso. A documentação de preços informa que dados enviados no nível gratuito podem ser usados para melhorar produtos Google. Não envie dados pessoais ou confidenciais dos turistas nesse nível. Consulte os links oficiais no contrato de integração antes de publicar.
