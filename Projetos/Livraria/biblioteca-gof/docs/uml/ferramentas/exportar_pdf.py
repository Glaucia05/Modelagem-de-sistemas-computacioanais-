from pathlib import Path
import argparse
from PIL import Image
from reportlab.pdfgen.canvas import Canvas

parser = argparse.ArgumentParser(description="Gerar PDF a partir das exportações oficiais PNG do Astah")
parser.add_argument("exportacao", type=Path)
parser.add_argument("saida", type=Path)
args = parser.parse_args()
arquivos = sorted(p for p in args.exportacao.rglob("*.png") if "Implementacao GoF" in str(p))
if len(arquivos) != 21:
    raise SystemExit(f"Esperados 21 diagramas atuais; encontrados {len(arquivos)}")
args.saida.parent.mkdir(parents=True, exist_ok=True)
c = Canvas(str(args.saida))
c.setTitle("Biblioteca GoF - classes, estados e sequências")
c.setAuthor("Sérgio Calazans - revisão com OpenAI Codex")
for numero, imagem in enumerate(arquivos, 1):
    with Image.open(imagem) as im:
        w, h = im.size
    escala = 0.75
    largura, altura = w * escala + 48, h * escala + 96
    c.setPageSize((largura, altura))
    c.setFont("Helvetica-Bold", 15)
    c.drawString(24, altura - 30, imagem.stem)
    c.setFont("Helvetica", 10)
    c.drawString(24, altura - 47, "Revisao da implementacao - exportacao do Astah UML - 21/11/2026")
    c.drawImage(str(imagem), 24, 32, width=w*escala, height=h*escala)
    c.setFont("Helvetica", 9)
    c.drawString(24, 16, f"{numero}/21 | Operacoes resumidas; consultar codigo e rastreabilidade para a API completa")
    c.showPage()
c.save()
print(f"PDF: {args.saida}; paginas: {len(arquivos)}")
