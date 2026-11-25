# Relatório de Refatoração e Qualidade

Este Pull Request aplica as correções solicitadas na disciplina de Engenharia de Qualidade de Software.

## O que foi corrigido:
1. **Números mágicos e strings fixas:** As coordenadas, textos e dimensões que estavam "chumbados" na `RoteiroDetalhadoActivity` foram extraídos para arquivos de recursos (`strings.xml`, `colors.xml`) e para classes de configuração (`CoordenadasSalvador`, `DimensoesRoteiro`).
2. **Princípio da Responsabilidade Única (SRP):** A lógica de dados foi removida da Activity e movida para classes de modelo (`ParadaRoteiro`, `RoteiroEssencialDados`, `ChavesApp`), deixando a tela apenas para controle de interface.
3. **Automação de Testes (CI):** Foi configurado um pipeline no GitHub Actions (`.github/workflows/android.yml`) que roda o build e os testes unitários a cada push ou pull request.

## Status do Pipeline
A execução do GitHub Actions está passando (Verde).
