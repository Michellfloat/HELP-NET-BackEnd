-- ===================================================================
-- Remocao de colunas orfas - PostgreSQL (profile prod)
--
-- Equivalente ao script MySQL. RODAR UMA VEZ por banco preexistente.
-- Ver o script MySQL para o contexto completo do problema.
-- ===================================================================

ALTER TABLE tab_usuarios DROP COLUMN IF EXISTS cadastro_completo;
ALTER TABLE tab_anexos   DROP COLUMN IF EXISTS caminho_arquivo;

-- ===================================================================
-- VERIFICACAO -- deve voltar VAZIA
-- ===================================================================
SELECT table_name, column_name
  FROM information_schema.columns
 WHERE table_schema = current_schema()
   AND ( (table_name = 'tab_usuarios' AND column_name = 'cadastro_completo')
      OR (table_name = 'tab_anexos'   AND column_name = 'caminho_arquivo') );

-- ===================================================================
-- NAO INCLUIDO: a conversao do enum ordinal de tab_escalonamento_logs.
-- Ver a nota no fim do script MySQL -- depende da anotacao
-- @Enumerated(EnumType.STRING) na entidade EscalonamentoLog.
-- ===================================================================
