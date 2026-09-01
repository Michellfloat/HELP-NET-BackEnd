-- ===================================================================
-- Remocao de colunas orfas - MySQL
--
-- RODAR UMA VEZ em cada banco que ja existia antes desta data.
-- Banco criado do zero pelo ddl-auto NAO precisa deste script.
--
-- CONTEXTO
-- spring.jpa.hibernate.ddl-auto=update CRIA coluna, mas nunca REMOVE.
-- Dois refactors tiraram campos das entidades e deixaram as colunas no
-- banco -- ambas NOT NULL e sem default. Como o Hibernate nao as envia
-- mais no INSERT, o MySQL em STRICT_TRANS_TABLES recusa a operacao
-- inteira. O efeito e que, em qualquer banco preexistente:
--
--   * nao e possivel cadastrar usuario
--   * nao e possivel enviar anexo
--
-- Reproducao do sintoma antes da correcao:
--
--   INSERT INTO tab_usuarios (nome,email,perfil,senha)
--   VALUES ('Teste','teste@helpdesk.com','USUARIO','x');
--   -- ERROR 1364: Field 'cadastro_completo' doesn't have a default value
--
--   INSERT INTO tab_anexos (nome_arquivo,tipo_arquivo,tamanho,data_upload,chamado_id)
--   VALUES ('t.png','image/png',10,NOW(),1);
--   -- ERROR 1364: Field 'caminho_arquivo' doesn't have a default value
-- ===================================================================

-- -------------------------------------------------------------------
-- 1) tab_usuarios.cadastro_completo
--    O campo cadastroCompleto saiu da entidade Usuario quando cargo e
--    setor viraram obrigatorios na criacao (commit 3be4e7f).
-- -------------------------------------------------------------------
ALTER TABLE tab_usuarios DROP COLUMN cadastro_completo;

-- -------------------------------------------------------------------
-- 2) tab_anexos.caminho_arquivo
--    Sobra da versao que gravava anexo em disco. O campo caminhoArquivo
--    esta comentado na entidade Anexo; os bytes vao em dados_arquivo.
-- -------------------------------------------------------------------
ALTER TABLE tab_anexos DROP COLUMN caminho_arquivo;

-- ===================================================================
-- VERIFICACAO -- as duas consultas abaixo devem voltar VAZIAS
-- ===================================================================
SELECT COLUMN_NAME, TABLE_NAME
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND ( (TABLE_NAME = 'tab_usuarios' AND COLUMN_NAME = 'cadastro_completo')
      OR (TABLE_NAME = 'tab_anexos'   AND COLUMN_NAME = 'caminho_arquivo') );

-- ===================================================================
-- NAO INCLUIDO NESTE SCRIPT
--
-- tab_escalonamento_logs.nivel_anterior e .novo_nivel estao como TINYINT
-- (ordinal 0/1/2) porque falta @Enumerated(EnumType.STRING) na entidade
-- EscalonamentoLog. A conversao dessas colunas EXISTE, mas so pode ser
-- aplicada JUNTO com a anotacao na entidade -- rodar o SQL sozinho
-- deixaria o banco em texto enquanto o codigo continua gravando numero.
-- Por isso ficou fora deste script, que trata apenas do bloqueio de INSERT.
-- ===================================================================
