import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import logo from '../assets/vladtechlogo.png';
import i18n from '../i18n';

const formatMoney = (amount, currency, locale = 'en-CA') => {
  if (amount === null || amount === undefined || amount === "") return 'N/A';
  const num = typeof amount === 'number' ? amount : Number(amount);
  if (Number.isNaN(num)) return 'N/A';
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency || 'CAD',
  }).format(num);
};

// Compatibility alias: some code imports `generatePdfBlob`.
export const generatePdfBlob = (projects, filename = 'estimate.pdf', options = {}) => {
  return generateEstimatePdfBlob(projects, filename, options);
};

const formatEmployees = (emails, noneLabel = '-') => {
  if (!emails || emails.length === 0) return noneLabel;
  return emails.filter(e => e && !e.startsWith('auth0|')).join(', ');
};

const formatAddress = (addr) => {
  if (!addr) return '-';
  const parts = [];
  if (addr.streetAddress) parts.push(addr.streetAddress);
  
  const cityProv = [addr.city, addr.province].filter(Boolean).join(', ');
  const cityProvPost = [cityProv, addr.postalCode].filter(Boolean).join(' ');
  
  if (cityProvPost) parts.push(cityProvPost);
  if (addr.country) parts.push(addr.country);
  return parts.length > 0 ? parts.join(', ') : '-';
};

const formatEstimatedTime = (seconds) => {
  if (!seconds || seconds <= 0) return '-';

  const SECONDS_IN_YEAR = 31536000;
  const SECONDS_IN_MONTH = 2592000;
  const SECONDS_IN_DAY = 86400;
  const SECONDS_IN_HOUR = 3600;

  let remaining = seconds;
  const parts = [];

  const years = Math.floor(remaining / SECONDS_IN_YEAR);
  if (years > 0) {
    parts.push(`${years}y`);
    remaining -= years * SECONDS_IN_YEAR;
  }

  const months = Math.floor(remaining / SECONDS_IN_MONTH);
  if (months > 0) {
    parts.push(`${months}mo`);
    remaining -= months * SECONDS_IN_MONTH;
  }

  const days = Math.floor(remaining / SECONDS_IN_DAY);
  if (days > 0) {
    parts.push(`${days}d`);
    remaining -= days * SECONDS_IN_DAY;
  }

  const hours = Math.floor(remaining / SECONDS_IN_HOUR);
  if (hours > 0) {
    parts.push(`${hours}h`);
  }

  return parts.length > 0 ? parts.join(' ') : '-';
};

export const generateCsv = (projects, filename = 'projects.csv', options = {}) => {
  if (!projects || projects.length === 0) {
    console.warn("generateCsv: No projects to export");
    return;
  }

  const { locale = 'en-CA' } = options;
  const lang = locale.split('-')[0];
  const t = (key) => i18n.t(key, { lng: lang });

  const headers = [
    t('project.id'), 
    t('project.projectName'), 
    t('project.client'), 
    'Client Email',
    t('project.employee'), 
    t('project.status'), 
    t('project.priority'), 
    t('project.projectType'), 
    t('project.estimatedCost'),
    'Estimated Time',
    t('project.startDate'),
    t('project.dueDate'),
    t('project.addressLabel')
  ];

  const rows = projects.map(p => {
    if (!p) return Array(headers.length).fill('');
    
    // Explicitly map IN_PROGRESS to the camelCase key used in i18n
    const mappedStatus = p.status === 'IN_PROGRESS' ? 'inProgress' : (p.status?.toLowerCase() || 'none');
    const statusKey = `project.${mappedStatus}`;
    
    const priorityKey = `project.priority${p.priority?.charAt(0).toUpperCase() + p.priority?.slice(1).toLowerCase() || 'none'}`;
    const typeKey = `project.${p.projectType?.toLowerCase() || 'none'}`;

    const address = formatAddress(p.address);
    
    return [
      p.projectIdentifier || '',
      p.name || '',
      p.clientName || '-',
      p.clientEmail || '-',
      formatEmployees(p.assignedEmployeeEmails, '-'),
      t(statusKey).toUpperCase(),
      t(priorityKey).toUpperCase(),
      t(typeKey),
      p.estimatedCost ? `${p.estimatedCost} ${p.estimatedCostCurrency || 'CAD'}` : '-',
      formatEstimatedTime(p.estimatedTime),
      p.startDate || '-',
      p.dueDate || '-',
      address
    ];
  });

  const csvString = [
    headers.join(','),
    ...rows.map(row => row.map(val => {
      const escaped = String(val).replace(/"/g, '""');
      return `"${escaped}"`;
    }).join(','))
  ].join('\n');

  const blob = new Blob(["\uFEFF" + csvString], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', filename);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
};

export const generatePdf = (projects, filename = 'projects.pdf', options = {}) => {
  if (!projects || projects.length === 0) {
    console.warn("generatePdf: No projects to export");
    return;
  }

  const { exporterName = 'System', title = 'Project Report', locale = 'en-CA', sortBy, sortOrder } = options;
  const t = (key, opts) => i18n.t(key, { ...opts, lng: locale.split('-')[0] });
  // note: labels are defined in estimate exporter when needed
  
  const doc = new jsPDF('p', 'mm', 'a4');
  doc.setCharSpace(0);
  const isSingle = projects.length === 1;
  const p = projects[0];

  // Header Design
  doc.setFillColor(20, 20, 20);
  doc.rect(0, 0, 210, 28, 'F');
  doc.addImage(logo, 'PNG', 11, 5, 45, 11);
  
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(11);
  doc.setFont("helvetica", "normal");
  doc.text(title, 14, 24);
  
  doc.setFontSize(8);
  doc.text(`${t('pdf.exportedBy')}: ${exporterName}`, 196, 11, { align: 'right' });
  doc.text(`${t('pdf.date')}: ${new Date().toLocaleString(locale, { timeZoneName: 'short' })}`, 196, 15, { align: 'right' });
  doc.text(`${t('pdf.totalProjects')}: ${projects.length}`, 196, 19, { align: 'right' });
  if (sortBy) {
    const orderStr = sortOrder === 'DESC' ? t('pdf.descending') : t('pdf.ascending');
    doc.text(`${t('pdf.sortedBy')}: ${sortBy} (${orderStr})`, 196, 23, { align: 'right' });
  }

  let startTableY = 35;

  // --- Premium Single Project Layout ---
  if (isSingle) {
    doc.setTextColor(40, 40, 40);
    
    // Project Name & ID
    doc.setFontSize(18);
    doc.setFont("helvetica", "bold");
    doc.text(p.name, 14, 45);
    
    doc.setFontSize(8);
    doc.setFont("helvetica", "normal");
    doc.setTextColor(100, 100, 100);
    doc.text(`${t('project.id')}: ${p.projectIdentifier}`, 14, 51);

    // Disclaimer
    doc.setFontSize(7);
    doc.setFont("helvetica", "italic");
    doc.text(t('pdf.disclaimer'), 14, 56);

    // Section 1: Overview
    doc.setDrawColor(230, 230, 230);
    doc.line(14, 62, 196, 62);
    
    doc.setTextColor(20, 20, 20);
    doc.setFontSize(10);
    doc.setFont("helvetica", "bold");
    doc.text(t('pdf.overview'), 14, 70);

    const mappedStatus = p.status === 'IN_PROGRESS' ? 'inProgress' : (p.status?.toLowerCase() || 'none');
    
    const overviewData = [
      [t('project.client'), p.clientName ? `${p.clientName} (${p.clientEmail || '-'})` : '-'],
      [t('project.status'), t(`project.${mappedStatus}`)],
      [t('project.priority'), t(`project.priority${p.priority?.charAt(0).toUpperCase() + p.priority?.slice(1).toLowerCase() || 'none'}`)],
      [t('project.projectType'), t(`project.${p.projectType?.toLowerCase() || 'none'}`)]
    ];

    autoTable(doc, {
      body: overviewData,
      startY: 74,
      margin: { left: 14 },
      theme: 'plain',
      styles: { fontSize: 9, cellPadding: 1 },
      columnStyles: { 0: { fontStyle: 'bold', cellWidth: 35, textColor: [100, 100, 100] } },
    });

    let currentY = doc.lastAutoTable.finalY + 10;

    // Section 1.5: Description
    if (p.description) {
      doc.setFont("helvetica", "bold");
      doc.setFontSize(10);
      doc.setTextColor(20, 20, 20);
      doc.text(t('pdf.projectDescription'), 14, currentY);
      
      doc.setFont("helvetica", "normal");
      doc.setFontSize(9);
      doc.setTextColor(50, 50, 50); // Slightly lighter to distinguish from older versions
      doc.setCharSpace(0);
      
      const maxWidth = 180; // Slightly narrower to be safe
      const cleanDesc = p.description.replace(/\s+/g, ' ').trim();
      const lines = doc.splitTextToSize(cleanDesc, maxWidth);
      
      let cursorY = currentY + 6;
      lines.forEach((line) => {
        // Explicitly set align 'left' for every line to override any justification
        doc.text(line, 14, cursorY, { align: 'left' });
        cursorY += 5; // Fixed 5mm line spacing
      });
      
      currentY = cursorY + 5;
    }

    // Section 2: Timeline & Financials
    doc.setFont("helvetica", "bold");
    doc.setFontSize(10);
    doc.setTextColor(20, 20, 20);
    doc.text(t('pdf.timeline'), 14, currentY);

    const timelineData = [
      [t('project.startDate'), p.startDate || '-'],
      [t('project.dueDate'), p.dueDate || '-'],
      [t('project.estimatedTime', { defaultValue: 'Estimated Time' }), formatEstimatedTime(p.estimatedTime)],
      [t('project.estimatedCost'), formatMoney(p.estimatedCost, p.estimatedCostCurrency, locale)],
      [t('project.addressLabel'), formatAddress(p.address)]
    ];

    autoTable(doc, {
      body: timelineData,
      startY: currentY + 4,
      margin: { left: 14 },
      theme: 'plain',
      styles: { fontSize: 9, cellPadding: 1 },
      columnStyles: { 0: { fontStyle: 'bold', cellWidth: 35, textColor: [100, 100, 100] } },
    });

    // Section 3: Personnel
    const personnelY = doc.lastAutoTable.finalY + 10;
    doc.setFont("helvetica", "bold");
    doc.text(t('pdf.personnel'), 14, personnelY);
    
    doc.setFont("helvetica", "normal");
    doc.setFontSize(9);
    doc.setCharSpace(0);
    doc.text(formatEmployees(p.assignedEmployeeEmails, '-'), 14, personnelY + 6, { maxWidth: 182 });

    startTableY = personnelY + 20;

    // Line separator before the summary table
    doc.setDrawColor(20, 20, 20);
    doc.setLineWidth(0.5);
    doc.line(14, startTableY - 5, 196, startTableY - 5);
  }

  // --- Table Layout ---
  const tableColumn = [
    t('project.id'), 
    t('project.projectName'), 
    t('project.client'), 
    t('project.employee'), 
    t('project.status'), 
    t('project.priority'), 
    t('project.projectType'), 
    t('project.estimatedCost'), 
    t('project.addressLabel')
  ];
  const tableRows = projects.map(proj => {
    const location = formatAddress(proj.address);
    const mappedStatus = proj.status === 'IN_PROGRESS' ? 'inProgress' : (proj.status?.toLowerCase() || 'none');

    return [
      proj.projectIdentifier || '',
      proj.name || '',
      proj.clientName || '-',
      formatEmployees(proj.assignedEmployeeEmails, '-'),
      t(`project.${mappedStatus}`),
      t(`project.priority${proj.priority?.charAt(0).toUpperCase() + proj.priority?.slice(1).toLowerCase() || 'none'}`),
      t(`project.${proj.projectType?.toLowerCase() || 'none'}`),
      formatMoney(proj.estimatedCost, proj.estimatedCostCurrency, locale),
      location,
    ];
  });

  autoTable(doc, {
    head: [tableColumn],
    body: tableRows,
    startY: startTableY,
    theme: 'striped',
    headStyles: { 
      fillColor: [20, 20, 20],
      textColor: [255, 255, 255],
      fontSize: 7.5,
      fontStyle: 'bold',
      halign: 'center',
      valign: 'middle',
      minCellHeight: 8
    },
    styles: { fontSize: 7, cellPadding: 1.5, overflow: 'linebreak' },
    columnStyles: {
      0: { cellWidth: 15 },                   // ID: Normal weight
      1: { cellWidth: 33 },                   // Project Name
      2: { cellWidth: 23 },                   // Client
      3: { cellWidth: 24 },                   // Employee
      4: { halign: 'center', cellWidth: 16 }, // Status
      5: { halign: 'center', cellWidth: 16 }, // Priority
      6: { halign: 'center', cellWidth: 17 }, // Type
      7: { halign: 'right', cellWidth: 12 },  // Cost
      8: { cellWidth: 28 },                   // Location
    },
    didParseCell: function(data) {
      if (data.section === 'body') {
        const rawProject = projects[data.row.index];
        if (!rawProject) return;

        const hexToRgb = (hex) => {
          const r = parseInt(hex.slice(1, 3), 16);
          const g = parseInt(hex.slice(3, 5), 16);
          const b = parseInt(hex.slice(5, 7), 16);
          return [r, g, b];
        };

        if (data.column.index === 0) data.cell.styles.textColor = [50, 50, 50];

        if (data.column.index === 4) {
          const status = rawProject.status;
          if (status === 'COMPLETED') data.cell.styles.textColor = hexToRgb('#047857');
          if (status === 'IN_PROGRESS') data.cell.styles.textColor = hexToRgb('#1d4ed8');
          if (status === 'PENDING') data.cell.styles.textColor = hexToRgb('#b45309');
        }

        if (data.column.index === 5) {
          const priority = rawProject.priority;
          if (priority === 'URGENT') data.cell.styles.textColor = hexToRgb('#991b1b');
          if (priority === 'HIGH') data.cell.styles.textColor = hexToRgb('#dc2626');
          if (priority === 'MEDIUM') data.cell.styles.textColor = hexToRgb('#ca8a04');
          if (priority === 'LOW') data.cell.styles.textColor = hexToRgb('#059669');
        }

        if (data.column.index === 6) data.cell.styles.textColor = [100, 100, 100];
      }
    },
    alternateRowStyles: { fillColor: [245, 245, 245] }
  });

  const totalPagesCount = doc.internal.getNumberOfPages();
  for (let i = 1; i <= totalPagesCount; i++) {
    doc.setPage(i);
    doc.setFontSize(8);
    doc.setTextColor(150);
    const pageText = t('pdf.pageOf', { current: i, total: totalPagesCount });
    doc.text(pageText, 105, 287, { align: 'center' });
    doc.text(`© ${new Date().getFullYear()} VladTech Inc. ${t('pdf.confidential')}`, 14, 287);
  }

  doc.save(filename);
};

export const generateEstimatePdfBlob = (projects, filename = 'estimate.pdf', options = {}) => {
  if (!projects || projects.length === 0) {
    console.warn('generateEstimatePdfBlob: No projects to export');
    return;
  }

  const { locale = 'en-CA', exporterName = 'System', title = 'Estimate' } = options;
  const t = (key, opts) => i18n.t(key, { ...opts, lng: locale.split('-')[0] });

  const labels = {
    materialCost: 'Material Cost',
    labor: 'Labor',
    applianceAllowance: 'Appliance Allowance',
    skylights: 'Skylights',
    tearOff: 'Tear Off',
    insulation: 'Insulation',
    subfloorRepair: 'Subfloor Repair',
    overhead: 'Overhead',
    contingency: 'Contingency',
    locationAdjustment: 'Location Adjustment',
    tax: 'Tax',
    estimatedTotal: 'Estimated Total'
  };
  const labelFor = (key) => labels[key] || (key ? key.replace(/([A-Z])/g, ' $1').replace(/^./, s => s.toUpperCase()) : key);

  const doc = new jsPDF('p', 'mm', 'a4');
  doc.setCharSpace(0);

  // attach filename to PDF metadata so param is used and eslint is satisfied
  try {
    doc.setProperties({ title: filename });
  } catch {
    // ignore if setProperties isn't supported in this environment
  }

  // Header (black) with logo and minimal meta
  doc.setFillColor(20, 20, 20);
  doc.rect(0, 0, 210, 28, 'F');
  doc.addImage(logo, 'PNG', 11, 5, 45, 11);

  doc.setTextColor(255, 255, 255);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'normal');
  doc.text(title, 14, 24);

  doc.setFontSize(8);
  doc.text(`${t('pdf.exportedBy')}: ${exporterName}`, 196, 11, { align: 'right' });
  doc.text(`${t('pdf.date')}: ${new Date().toLocaleString(locale, { timeZoneName: 'short' })}`, 196, 15, { align: 'right' });
  // Intentionally do NOT show total projects here for estimates

  let cursorY = 35;

  projects.forEach((p, idx) => {
    const projectTitle = p.name || `Estimate ${idx + 1}`;
    if (projects.length > 1) {
      doc.setTextColor(40, 40, 40);
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text(projectTitle, 14, cursorY);
      cursorY += 8;
    }

    // Render a small "Selections" table listing form choices (area, materials, options)
    const selections = [];
    const pushSelection = (label, value) => {
      if (value === undefined || value === null) return;
      const str = typeof value === 'boolean' ? (value ? 'Yes' : 'No') : String(value);
      if (str.trim() === '') return;
      selections.push([label, str]);
    };

    // Area / square footage
    const areaLabel = 'Area (sq ft)';
    const areaVal = p.areaSqFt ?? p.squareFeet ?? p.deckAreaSqFt ?? p.area;
    if (areaVal !== undefined) pushSelection(areaLabel, areaVal);

    // Common material/choice fields
    pushSelection('Siding Material', p.sidingMaterial);
    pushSelection('Roof Material', p.roofMaterial);
    pushSelection('Countertop Material', p.countertopMaterial);
    pushSelection('New Floor Material', p.newFloorMaterial);
    pushSelection('Deck Material', p.deckMaterial);
    pushSelection('Cabinet Quality', p.cabinetQuality);
    pushSelection('Flooring Material', p.flooringMaterial);
    pushSelection('Window Type', p.windowType);
    pushSelection('Door Type', p.doorType);
    pushSelection('Stories', p.stories);
    pushSelection('Has Railing', p.hasRailing);
    pushSelection('Stairs Count', p.stairsCount);
    pushSelection('Is Covered', p.isCovered);
    pushSelection('Include Insulation', p.includeInsulation);
    pushSelection('Tear Off Required', p.tearOffRequired);
    pushSelection('Number of Skylights', p.numSkylights);
    pushSelection('Appliance Allowance', p.applianceAllowance);

    if (selections.length > 0) {
      autoTable(doc, {
        head: [[ 'Selection', 'Value' ]],
        body: selections,
        startY: cursorY,
        margin: { left: 14, right: 14 },
        theme: 'plain',
        headStyles: { fillColor: [245, 245, 245], textColor: [40, 40, 40], fontStyle: 'bold' },
        styles: { fontSize: 9 },
        columnStyles: { 0: { cellWidth: 100 }, 1: { halign: 'left' } }
      });

      cursorY = doc.lastAutoTable.finalY + 6;
    }

    // Build cost breakdown rows tailored per preset/projectType and omit empty values
    const rows = [];
    const area = p.areaSqFt ?? p.squareFeet ?? p.deckAreaSqFt ?? p.area;
    const areaNum = area ? Number(area) : NaN;
    const estimatePriceNum = Number(p.estimatePrice ?? p.estimatedPrice ?? p.estimate ?? 0) || 0;
    const overheadRate = Number(p.overheadRate ?? 0);
    const contingencyRate = Number(p.contingencyRate ?? 0);

    const pushIf = (label, value) => {
      if (value === undefined || value === null) return;
      if (typeof value === 'number' && Number.isNaN(value)) return;
      if (String(value).trim() === '') return;
      rows.push([label, String(value)]);
    };

    const currency = p.estimatedCostCurrency || 'CAD';

    const type = (p.projectType || 'GENERAL').toUpperCase();
    if (type === 'SIDING_REPLACE') {
      if (!isNaN(areaNum) && p.materialCostPerSqFt) {
        pushIf(labelFor('materialCost'), formatMoney(Number(p.materialCostPerSqFt) * areaNum, currency, locale));
      }
      if (!isNaN(areaNum) && p.laborRate) {
        pushIf(labelFor('labor'), formatMoney(Number(p.laborRate) * areaNum, currency, locale));
      }
      if (p.includeInsulation) pushIf(labelFor('insulation'), formatMoney(areaNum * 0.75, currency, locale));
      if (p.locationFactor && Number(p.locationFactor) !== 1) pushIf(labelFor('locationAdjustment'), `${((Number(p.locationFactor) - 1) * 100).toFixed(1)}%`);
    } else if (type === 'ROOFING_REPLACE') {
      if (!isNaN(areaNum) && p.materialCostPerSqFt) pushIf(labelFor('materialCost'), formatMoney(Number(p.materialCostPerSqFt) * areaNum, currency, locale));
      if (!isNaN(areaNum) && p.laborRate) pushIf(labelFor('labor'), formatMoney(Number(p.laborRate) * areaNum, currency, locale));
      if (p.numSkylights && Number(p.numSkylights) > 0) pushIf(`${labelFor('skylights')} (${p.numSkylights})`, formatMoney(Number(p.numSkylights) * 1000, currency, locale));
      if (p.tearOffRequired) pushIf(labelFor('tearOff'), formatMoney(areaNum * 1.5, currency, locale));
    } else if (type === 'KITCHEN_REMODEL') {
      if (!isNaN(areaNum) && p.materialCostPerSqFt) pushIf(labelFor('materialCost'), formatMoney(Number(p.materialCostPerSqFt) * areaNum, currency, locale));
      if (!isNaN(areaNum) && p.laborRate) pushIf(labelFor('labor'), formatMoney(Number(p.laborRate) * areaNum, currency, locale));
      if (p.applianceAllowance && Number(p.applianceAllowance) > 0) pushIf(labelFor('applianceAllowance'), formatMoney(Number(p.applianceAllowance), currency, locale));
    } else if (type === 'DECK_PATIO_ADDITION') {
      if (!isNaN(areaNum) && p.materialCostPerSqFt) pushIf(labelFor('materialCost'), formatMoney(Number(p.materialCostPerSqFt) * areaNum, currency, locale));
      if (!isNaN(areaNum) && p.laborRate) pushIf(labelFor('labor'), formatMoney(Number(p.laborRate) * areaNum, currency, locale));
    } else {
      // GENERAL fallback: mirror modal's visible breakdown logic
      if (!isNaN(areaNum) && p.materialCostPerSqFt) pushIf(labelFor('materialCost'), formatMoney(Number(p.materialCostPerSqFt) * areaNum, currency, locale));
      if (!isNaN(areaNum) && p.laborRate) pushIf(labelFor('labor'), formatMoney(Number(p.laborRate) * areaNum, currency, locale));
      if (p.applianceAllowance && Number(p.applianceAllowance) > 0) pushIf(labelFor('applianceAllowance'), formatMoney(Number(p.applianceAllowance), currency, locale));
      if (p.numSkylights && Number(p.numSkylights) > 0) pushIf(`${labelFor('skylights')} (${p.numSkylights})`, formatMoney(Number(p.numSkylights) * 1000, currency, locale));
      if (p.tearOffRequired) pushIf(labelFor('tearOff'), formatMoney(areaNum * 1.5, currency, locale));
      if (p.includeInsulation) pushIf(labelFor('insulation'), formatMoney(areaNum * 0.75, currency, locale));
      if (p.subfloorRepairNeeded) pushIf(labelFor('subfloorRepair'), formatMoney(areaNum * 3.5, currency, locale));
    }

    // Compute numeric pieces for total calculation
    const matNum = !isNaN(areaNum) && p.materialCostPerSqFt ? (areaNum * Number(p.materialCostPerSqFt || 0)) : 0;
    const labNum = !isNaN(areaNum) && p.laborRate ? (areaNum * Number(p.laborRate || 0)) : 0;
    const applianceNum = p.applianceAllowance ? Number(p.applianceAllowance || 0) : 0;
    const skylightNum = p.numSkylights ? (Number(p.numSkylights || 0) * 1000) : 0;
    const tearOffNum = p.tearOffRequired && !isNaN(areaNum) ? (areaNum * 1.5) : 0;
    const insulationNum = p.includeInsulation && !isNaN(areaNum) ? (areaNum * 0.75) : 0;
    const subfloorNum = p.subfloorRepairNeeded && !isNaN(areaNum) ? (areaNum * 3.5) : 0;

    const overheadAmt = overheadRate ? (estimatePriceNum * overheadRate) / (1 + overheadRate + (contingencyRate || 0)) : 0;
    const contingencyAmt = contingencyRate ? (estimatePriceNum * contingencyRate) / (1 + overheadRate + contingencyRate) : 0;
    const taxNum = p.taxAmount ? Number(p.taxAmount || 0) : 0;

    // Compute numeric total: prefer explicit `totalPrice`, else derive from available parts
    let totalValue = 0;
    if (p.totalPrice !== undefined && p.totalPrice !== null) {
      totalValue = Number(p.totalPrice) || 0;
    } else if (p.estimatePrice !== undefined && p.estimatePrice !== null) {
      // If estimatePrice looks like the subtotal (pre-tax), add computed pieces and tax; otherwise fall back to estimatePrice + tax
      if (estimatePriceNum > 0) {
        totalValue = estimatePriceNum + taxNum; // best effort
      } else {
        totalValue = matNum + labNum + applianceNum + skylightNum + tearOffNum + insulationNum + subfloorNum + overheadAmt + contingencyAmt + taxNum;
      }
    } else if (Array.isArray(p.costBreakdown) && p.costBreakdown.length > 0) {
      totalValue = p.costBreakdown.reduce((s, it) => s + (Number(it.amount ?? it.value ?? 0) || 0), 0);
    }

    // Overhead / Contingency / Tax / Location (push into rows for visible breakdown)
    if (overheadRate) {
      pushIf(`${labelFor('overhead')} (${(overheadRate * 100).toFixed(1)}%)`, formatMoney(overheadAmt, currency, locale));
    }
    if (contingencyRate) {
      pushIf(`${labelFor('contingency')} (${(contingencyRate * 100).toFixed(1)}%)`, formatMoney(contingencyAmt, currency, locale));
    }
    if (p.locationFactor && Number(p.locationFactor) && Number(p.locationFactor) !== 1) pushIf(labelFor('locationAdjustment'), `${((Number(p.locationFactor) - 1) * 100).toFixed(1)}%`);
    if (p.taxRate) pushIf(`${labelFor('tax')} (${(Number(p.taxRate) * 100).toFixed(1)}%)`, formatMoney(taxNum, currency, locale));

    // Always include an Estimated Total row (visible in the table)
    pushIf(labelFor('estimatedTotal'), formatMoney(totalValue, currency, locale));

    // Render table: two columns (Description, Amount)
    autoTable(doc, {
      head: [[ 'Description', 'Amount' ]],
      body: rows,
      startY: cursorY,
      margin: { left: 14, right: 14 },
      theme: 'plain',
      headStyles: { fillColor: [245, 245, 245], textColor: [40, 40, 40], fontStyle: 'bold' },
      styles: { fontSize: 9 },
      columnStyles: { 0: { cellWidth: 140 }, 1: { halign: 'right' } }
    });

    cursorY = doc.lastAutoTable.finalY + 6;

    // Add page if running out of space
    if (idx < projects.length - 1 && cursorY > 240) {
      doc.addPage();
      cursorY = 20;
    }
  });

  // Return blob instead of saving directly
  try {
    const blob = doc.output('blob');
    return blob;
  } catch (e) {
    console.error('generateEstimatePdfBlob: failed to create blob', e);
    return null;
  }
};
