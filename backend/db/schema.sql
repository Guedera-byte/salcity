-- =====================================================================
-- SalCity - Esquema do banco de dados (SQLite 3.37 ou superior)
-- Implementa o DER da seção 10 da Documentação de Requisitos.
--
-- O banco guarda somente:
--   * controle de acesso e LGPD: usuario, token_seguranca, consentimento
--   * dados do usuário: item_agenda (roteiros e eventos salvos, em JSON)
--   * cache das respostas da IA: cache_ia
-- Locais turísticos vêm do Google Maps/Places e NÃO são armazenados.
--
-- Uso:  sqlite3 salcity.db < schema.sql   (ou pelo backend na inicialização)
-- O script pode ser executado mais de uma vez (idempotente).
--
-- CONVENÇÕES
-- * Tabelas STRICT: o SQLite passa a recusar valores do tipo errado.
-- * Datas: TEXT em ISO 8601, UTC, sempre no formato
--     YYYY-MM-DDTHH:MM:SS.SSSZ   (o mesmo de new Date().toISOString())
--   Com um único formato, as datas podem ser comparadas como texto.
-- * Booleanos: INTEGER 0 ou 1.
-- * JSON: TEXT validado com json_valid() e json_type().
--
-- IMPORTANTE PARA O BACKEND (a cada conexão aberta):
--   PRAGMA foreign_keys = ON;   -- sem isso, FKs e ON DELETE CASCADE NÃO funcionam
--   PRAGMA busy_timeout = 5000; -- espera em vez de falhar quando outro processo grava
-- =====================================================================

PRAGMA journal_mode = WAL;   -- leituras simultâneas durante gravações (fica salvo no arquivo)
PRAGMA foreign_keys = ON;

BEGIN;

-- ---------------------------------------------------------------------
-- USUARIO (RF01, RF03, RF04, RF07, RF08, RF10; RN-01, RN-23)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id                      INTEGER PRIMARY KEY,
    nome                    TEXT    NOT NULL CHECK (length(nome) BETWEEN 1 AND 120),
    email                   TEXT    NOT NULL CHECK (length(email) <= 254 AND email = lower(email)),
    senha_hash              TEXT,                    -- bcrypt ou Argon2; nulo se a conta usa só o Google
    google_sub              TEXT,                    -- identificador da conta Google (claim "sub")
    telefone                TEXT    CHECK (telefone IS NULL OR length(telefone) <= 20),
    foto_url                TEXT    CHECK (foto_url IS NULL OR length(foto_url) <= 500),
    status                  TEXT    NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'EXCLUSAO_SOLICITADA')),
    exclusao_solicitada_em  TEXT    CHECK (exclusao_solicitada_em IS NULL OR (length(exclusao_solicitada_em) = 24 AND exclusao_solicitada_em GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z')),
    tentativas_login_falhas INTEGER NOT NULL DEFAULT 0 CHECK (tentativas_login_falhas >= 0),
    bloqueado_ate           TEXT    CHECK (bloqueado_ate IS NULL OR (length(bloqueado_ate) = 24 AND bloqueado_ate GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z')),
    criado_em               TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    atualizado_em           TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),

    CONSTRAINT uq_usuario_email      UNIQUE (email),
    CONSTRAINT uq_usuario_google_sub UNIQUE (google_sub),
    CONSTRAINT ck_usuario_forma_acesso CHECK (senha_hash IS NOT NULL OR google_sub IS NOT NULL),
    CONSTRAINT ck_usuario_exclusao     CHECK ((status = 'EXCLUSAO_SOLICITADA') = (exclusao_solicitada_em IS NOT NULL))
) STRICT;

-- ---------------------------------------------------------------------
-- TOKEN_SEGURANCA (RF05, RF06)
-- SESSAO = token de renovação da sessão; RESET_SENHA = link de redefinição.
-- Guarda apenas o hash SHA-256 (64 caracteres hexadecimais) do token.
-- usado_em preenchido = token usado (redefinição) ou revogado (logout).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS token_seguranca (
    id          INTEGER PRIMARY KEY,
    usuario_id  INTEGER NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    tipo        TEXT    NOT NULL CHECK (tipo IN ('SESSAO', 'RESET_SENHA')),
    token_hash  TEXT    NOT NULL CHECK (length(token_hash) = 64 AND token_hash NOT GLOB '*[^0-9a-f]*'),
    criado_em   TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    expira_em   TEXT    NOT NULL CHECK (length(expira_em) = 24 AND expira_em GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z'),
    usado_em    TEXT    CHECK (usado_em IS NULL OR (length(usado_em) = 24 AND usado_em GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z')),

    CONSTRAINT uq_token_hash      UNIQUE (token_hash),
    CONSTRAINT ck_token_expiracao CHECK (expira_em > criado_em),
    CONSTRAINT ck_token_uso       CHECK (usado_em IS NULL OR usado_em >= criado_em)
) STRICT;
CREATE INDEX IF NOT EXISTS ix_token_usuario_tipo ON token_seguranca (usuario_id, tipo);

-- ---------------------------------------------------------------------
-- CONSENTIMENTO (RF02, RF09, RF11, RF12; RN-19)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consentimento (
    id                INTEGER PRIMARY KEY,
    usuario_id        INTEGER NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    tipo              TEXT    NOT NULL CHECK (tipo IN ('TERMOS_USO', 'POLITICA_PRIVACIDADE', 'LOCALIZACAO', 'NOTIFICACOES')),
    versao_documento  TEXT    CHECK (versao_documento IS NULL OR length(versao_documento) <= 20),
    aceito_em         TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    revogado_em       TEXT    CHECK (revogado_em IS NULL OR (length(revogado_em) = 24 AND revogado_em GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z')),

    CONSTRAINT ck_consentimento_versao    CHECK (tipo NOT IN ('TERMOS_USO', 'POLITICA_PRIVACIDADE') OR versao_documento IS NOT NULL),
    CONSTRAINT ck_consentimento_revogacao CHECK (revogado_em IS NULL OR revogado_em >= aceito_em)
) STRICT;
-- No máximo um consentimento ativo (não revogado) por usuário e tipo.
CREATE UNIQUE INDEX IF NOT EXISTS uq_consentimento_ativo
    ON consentimento (usuario_id, tipo) WHERE revogado_em IS NULL;

-- ---------------------------------------------------------------------
-- ITEM_AGENDA (RF35 a RF44; RN-09, RN-15 a RN-18, RN-31)
-- Um roteiro ou um evento salvo pelo usuário. O conteúdo completo
-- (JSON devolvido pela IA) fica em "conteudo".
-- excluido_em: exclusão lógica (histórico e sincronização da remoção).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_agenda (
    id                  INTEGER PRIMARY KEY,
    usuario_id          INTEGER NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    tipo                TEXT    NOT NULL CHECK (tipo IN ('ROTEIRO', 'EVENTO')),
    titulo              TEXT    NOT NULL CHECK (length(titulo) BETWEEN 1 AND 200),
    inicio              TEXT    NOT NULL CHECK (length(inicio) = 24 AND inicio GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z'),
    fim                 TEXT    NOT NULL CHECK (length(fim) = 24 AND fim GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z'),
                                             -- evento sem término na fonte: inicio + 2 h
    conteudo            TEXT    NOT NULL CHECK (json_valid(conteudo) AND json_type(conteudo) = 'object'),
    url_fonte           TEXT    CHECK (url_fonte IS NULL OR length(url_fonte) <= 500),
    favorito            INTEGER NOT NULL DEFAULT 0 CHECK (favorito IN (0, 1)),
    lembrete_min_antes  INTEGER CHECK (lembrete_min_antes IS NULL OR lembrete_min_antes > 0),
    versao              INTEGER NOT NULL DEFAULT 1 CHECK (versao >= 1),
    criado_em           TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    atualizado_em       TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    excluido_em         TEXT    CHECK (excluido_em IS NULL OR (length(excluido_em) = 24 AND excluido_em GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z')),

    CONSTRAINT ck_item_periodo      CHECK (fim >= inicio),
    CONSTRAINT ck_item_duracao_rot  CHECK (tipo <> 'ROTEIRO' OR julianday(fim) - julianday(inicio) <= 31),
    CONSTRAINT ck_item_fonte_evento CHECK (tipo <> 'EVENTO' OR url_fonte IS NOT NULL)
) STRICT;
-- Calendário do mês e detecção de conflito (itens ativos).
CREATE INDEX IF NOT EXISTS ix_item_usuario_inicio
    ON item_agenda (usuario_id, inicio) WHERE excluido_em IS NULL;
-- Sincronização: itens alterados (inclusive excluídos) desde a última sincronização.
CREATE INDEX IF NOT EXISTS ix_item_usuario_atualizado
    ON item_agenda (usuario_id, atualizado_em);
-- Impede salvar o mesmo evento duas vezes (UC Gerenciar calendário, E.2).
CREATE UNIQUE INDEX IF NOT EXISTS uq_item_evento_ativo
    ON item_agenda (usuario_id, titulo, inicio) WHERE tipo = 'EVENTO' AND excluido_em IS NULL;

-- ---------------------------------------------------------------------
-- CACHE_IA (RF24; RNF29, RNF55, RNF63 a RNF65)
-- chave = SHA-256 dos parâmetros normalizados (tipo, período, temas em
-- ordem alfabética). Não há coluna que identifique o usuário.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cache_ia (
    chave          TEXT NOT NULL PRIMARY KEY CHECK (length(chave) = 64 AND chave NOT GLOB '*[^0-9a-f]*'),
    tipo           TEXT NOT NULL CHECK (tipo IN ('BUSCA_EVENTOS', 'GERAR_ROTEIRO')),
    parametros     TEXT NOT NULL CHECK (json_valid(parametros)),
    resposta       TEXT NOT NULL CHECK (json_valid(resposta) AND json_type(resposta) = 'object'),
    modelo         TEXT NOT NULL CHECK (length(modelo) <= 60),          -- ex.: gemini-3.5-flash-lite
    versao_prompt  TEXT NOT NULL CHECK (length(versao_prompt) <= 20),
    criado_em      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    expira_em      TEXT NOT NULL CHECK (length(expira_em) = 24 AND expira_em GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9][0-9]Z'),

    CONSTRAINT ck_cache_expiracao CHECK (expira_em > criado_em)
) STRICT, WITHOUT ROWID;
CREATE INDEX IF NOT EXISTS ix_cache_expira ON cache_ia (expira_em);

-- ---------------------------------------------------------------------
-- Gatilhos (RN-18)
-- O SQLite não permite alterar NEW em gatilhos BEFORE; por isso os
-- gatilhos são AFTER UPDATE e fazem um segundo UPDATE na mesma linha.
-- A condição WHEN evita que esse segundo UPDATE dispare o gatilho de novo.
-- O backend nunca deve alterar "versao" diretamente.
-- ---------------------------------------------------------------------
CREATE TRIGGER IF NOT EXISTS tg_usuario_atualizado
AFTER UPDATE ON usuario
FOR EACH ROW WHEN NEW.atualizado_em = OLD.atualizado_em
BEGIN
    UPDATE usuario SET atualizado_em = strftime('%Y-%m-%dT%H:%M:%fZ', 'now') WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS tg_item_agenda_versao
AFTER UPDATE ON item_agenda
FOR EACH ROW WHEN NEW.versao = OLD.versao
BEGIN
    UPDATE item_agenda
       SET versao = OLD.versao + 1,
           atualizado_em = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
     WHERE id = NEW.id;
END;

COMMIT;

-- =====================================================================
-- Consultas de referência para o backend (não executadas pelo script)
-- Parâmetros nomeados no estilo do better-sqlite3 / node:sqlite.
-- =====================================================================
-- Itens do calendário de um mês ($ini = 1º dia do mês, $fimMes = 1º dia do mês seguinte):
--   SELECT * FROM item_agenda
--    WHERE usuario_id = $u AND excluido_em IS NULL
--      AND inicio < $fimMes AND fim >= $ini
--    ORDER BY inicio;
--
-- Conflito de agenda (RF41): itens ativos que se sobrepõem ao novo período.
--   SELECT id, tipo, titulo, inicio, fim FROM item_agenda
--    WHERE usuario_id = $u AND excluido_em IS NULL
--      AND inicio < $novoFim AND fim > $novoInicio
--      AND id <> coalesce($idEmEdicao, -1);
--
-- Atualização com controle de versão (RF44, RN-18): 0 linhas = conflito.
--   UPDATE item_agenda SET inicio = $inicio, fim = $fim
--    WHERE id = $id AND usuario_id = $u AND versao = $versaoConhecida;
--
-- Exclusão lógica (RF39):
--   UPDATE item_agenda SET excluido_em = strftime('%Y-%m-%dT%H:%M:%fZ','now')
--    WHERE id = $id AND usuario_id = $u;
--
-- Sincronização (RF44): alterações desde a última sincronização do aparelho.
--   SELECT * FROM item_agenda WHERE usuario_id = $u AND atualizado_em > $ultimaSync;
--
-- Cache (RF24):
--   SELECT resposta, criado_em FROM cache_ia
--    WHERE chave = $chave AND expira_em > strftime('%Y-%m-%dT%H:%M:%fZ','now');
