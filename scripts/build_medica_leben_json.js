const fs = require('fs');
const path = require('path');

const sourcePath = path.join(__dirname, 'medica_leben_source.txt');
const targetPath = path.join(__dirname, '..', 'src', 'main', 'resources', 'seed', 'medica_leben_survey.json');

function foldLines(lines) {
  const folded = [];
  let buffer = '';
  lines.forEach(rawLine => {
    const line = rawLine.trim();
    if (!line) return;
    if (/^\d+\.-/.test(line)) {
      if (buffer) folded.push(buffer.trim());
      buffer = line;
    } else if (buffer) {
      buffer += ' ' + line;
    }
  });
  if (buffer) folded.push(buffer.trim());
  return folded;
}

function inferCategory(number) {
  if (number <= 22) return 'Datos generales';
  if (number <= 44) return 'Ambiente laboral';
  if (number <= 76) return 'Condiciones laborales';
  if (number <= 105) return 'Hábitos y salud';
  if (number <= 135) return 'Síntomas y dolor';
  return 'Eventos críticos';
}

function sanitizeOption(text) {
  return text
    .replace(/^[-–•]+/, '')
    .replace(/:+$/, '')
    .replace(/\s+/g, ' ')
    .trim();
}

function splitOptions(raw) {
  if (!raw) return [];
  const cleaned = raw
    .replace(/\s{2,}/g, ' ')
    .replace(/\s*;\s*/g, ',');
  const parts = cleaned
    .split(',')
    .map(part => sanitizeOption(part))
    .filter(Boolean);
  const expanded = [];
  const hasLetters = (value) => /[a-záéíóúñ]/i.test(value);
  parts.forEach(part => {
    if (part.includes(' o ') && !/\([^)]*o[^)]*\)/i.test(part)) {
      const maybe = part.split(' o ').map(p => sanitizeOption(p));
      if (maybe.length > 1 && maybe.every(chunk => hasLetters(chunk))) {
        expanded.push(...maybe.filter(Boolean));
        return;
      }
    }
    expanded.push(part);
  });
  return expanded;
}

function determineType(prompt, options, matrixRows) {
  const questionText = prompt.toLowerCase();
  if (matrixRows.length && options.length) return 'matrix';
  if (!options.length && matrixRows.length) return 'multi_select';
  if (!options.length) {
    if (questionText.includes('fecha')) return 'date';
    if (questionText.includes('hora de inicio') || questionText.includes('hora de término')) return 'time';
    return 'text';
  }
  const multiKeywords = /(seleccione|marque|indique|señala|selecciona|selecciona la última)/i;
  if (multiKeywords.test(prompt) || prompt.toLowerCase().includes('señala si habitualmente')) {
    return 'multi_select';
  }
  return 'single_choice';
}

function buildQuestions(lines) {
  return lines.map(line => {
    const match = line.match(/^(\d+)\.-\s*(.+)$/);
    if (!match) {
      throw new Error(`No se pudo analizar la línea: ${line}`);
    }
    const number = parseInt(match[1], 10);
    let body = match[2].trim();
    let prompt = body;
    let tail = '';
    const colonIndex = body.indexOf(':');
    if (colonIndex >= 0) {
      prompt = body.slice(0, colonIndex).trim();
      tail = body.slice(colonIndex + 1).trim();
    }

    let matrixRows = [];
    const otherPrompts = [];
    let workingTail = tail;

    workingTail = workingTail.replace(/\[([^\]]+)\]/g, (_, inner) => {
      const rows = inner.split(',').map(v => sanitizeOption(v)).filter(Boolean);
      if (rows.length) matrixRows.push(...rows);
      return ' ';
    });

    workingTail = workingTail.replace(/\{([^}]+)\}/g, (_, inner) => {
      const cleaned = inner.replace(/_{2,}/g, '').trim();
      if (cleaned) otherPrompts.push(cleaned);
      return ' ';
    });

    workingTail = workingTail.replace(/^:+/, '').trim();
    const options = splitOptions(workingTail);

    // Determine if extracted matrix rows are actual rows or option set
    let effectiveMatrixRows = matrixRows.slice();
    let effectiveOptions = options.slice();
    if (effectiveOptions.length === 0 && effectiveMatrixRows.length > 0 && tail.trim().startsWith('[')) {
      effectiveOptions = effectiveMatrixRows;
      effectiveMatrixRows = [];
    }

    const type = determineType(prompt, effectiveOptions, effectiveMatrixRows);
    const allowMultiple = type === 'multi_select' || type === 'matrix';

    const metadata = {};
    const hasOtrosPrompt = otherPrompts.some(promptText => promptText.toLowerCase().startsWith('otros'));
    if (hasOtrosPrompt && effectiveOptions.length && type !== 'matrix') {
      effectiveOptions.push('Otros');
      metadata.otherOption = true;
    }
    if (effectiveMatrixRows.length) metadata.rows = effectiveMatrixRows;
    if (effectiveOptions.length === 0 && type === 'matrix') {
      metadata.columns = splitOptions(tail.split('[')[0] || '');
    }
    if (type === 'matrix') {
      if (!metadata.columns || !metadata.columns.length) {
        metadata.columns = effectiveOptions.length ? effectiveOptions : [];
      }
      metadata.selection = 'checkbox';
    }
    if (otherPrompts.length) {
      metadata.otherPrompts = otherPrompts;
    }

    const question = {
      number,
      text: prompt,
      type,
      category: inferCategory(number),
      allowMultiple,
      options: type === 'matrix' ? [] : effectiveOptions,
      metadata: Object.keys(metadata).length ? metadata : undefined
    };
    return question;
  });
}

function main() {
  if (!fs.existsSync(sourcePath)) {
    console.error(`No se encontró ${sourcePath}`);
    process.exit(1);
  }
  const rawLines = fs.readFileSync(sourcePath, 'utf8').split(/\r?\n/);
  const folded = foldLines(rawLines);
  const questions = buildQuestions(folded);

  const output = {
    code: 'MEDICA_LEBEN',
    title: 'Encuesta Médica Leben',
    description: 'Instrumento de 183 reactivos que combina antecedentes sociodemográficos, condiciones laborales y salud integral.',
    guideType: 'Personalizado',
    totalQuestions: questions.length,
    generatedAt: new Date().toISOString(),
    questions
  };

  const targetDir = path.dirname(targetPath);
  if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
  }

  fs.writeFileSync(targetPath, JSON.stringify(output, null, 2), 'utf8');
  console.log(`Archivo generado en ${targetPath} con ${questions.length} preguntas.`);
}

main();
