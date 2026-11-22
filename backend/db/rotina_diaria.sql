-- =====================================================================
-- SalCity - Rotina diária do banco (RN-05, RN-23)
-- O SQLite não tem funções armazenadas; o backend executa este bloco
-- uma vez por dia (por exemplo, com um agendador no próprio servidor).
--
-- Uso manual:  sqlite3 salcity.db < rotina_diaria.sql
-- =====================================================================
PRAGMA foreign_keys = ON;   -- necessário para a exclusão em cascata das contas

BEGIN;

-- Cache da IA vencido
DELETE FROM cache_ia
 WHERE expira_em <= strftime('%Y-%m-%dT%H:%M:%fZ', 'now');

-- Tokens vencidos há mais de 7 dias
DELETE FROM token_seguranca
 WHERE expira_em <= strftime('%Y-%m-%dT%H:%M:%fZ', 'now', '-7 days');

-- Contas com exclusão solicitada (tokens, consentimentos e itens saem em cascata)
DELETE FROM usuario
 WHERE status = 'EXCLUSAO_SOLICITADA';

COMMIT;
