import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PromptService, PromptRequest, PromptResponse, Template } from './prompt.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly promptService = inject(PromptService);

  // Categories
  readonly categories = ['Code', 'Documentation', 'Testing', 'Design'];
  selectedCategory = signal<string>('Code');

  // Templates
  allTemplates = signal<Template[]>([]);
  filteredTemplates = computed(() => {
    return this.allTemplates().filter(t => t.category === this.selectedCategory());
  });

  // Editor Inputs
  inputPrompt = signal<string>('');
  activeTemplateTitle = signal<string>('Custom Prompt');

  // Load States
  isLoading = signal<boolean>(false);
  isSavingReview = signal<boolean>(false);

  // History & Active Run
  historyList = signal<PromptResponse[]>([]);
  selectedHistoryCategory = signal<string>('All');
  
  filteredHistoryList = computed(() => {
    const category = this.selectedHistoryCategory();
    if (category === 'All') {
      return this.historyList();
    }
    return this.historyList().filter(item => item.category.toLowerCase() === category.toLowerCase());
  });

  activeExecution = signal<PromptResponse | null>(null);

  // Review fields for Active Run
  reviewNotes = signal<string>('');
  rating = signal<number | null>(null); // 1 = helpful, -1 = not helpful

  // Versioning and Comparison
  relatedVersions = signal<PromptResponse[]>([]); // All versions of current activePrompt
  comparisonRun = signal<PromptResponse | null>(null); // The run we are comparing the active run with
  isComparing = signal<boolean>(false);

  ngOnInit() {
    this.loadTemplates();
    this.loadHistory();
  }

  loadTemplates() {
    this.promptService.getTemplates().subscribe({
      next: (data) => {
        this.allTemplates.set(data);
        // Select first template of selected category by default
        const defaultTemplate = data.find(t => t.category === this.selectedCategory());
        if (defaultTemplate) {
          this.selectTemplate(defaultTemplate);
        }
      },
      error: (err) => console.error('Error fetching templates:', err)
    });
  }

  loadHistory() {
    this.promptService.getHistory().subscribe({
      next: (data) => this.historyList.set(data),
      error: (err) => console.error('Error fetching history:', err)
    });
  }

  selectCategory(category: string) {
    this.selectedCategory.set(category);
    // Auto select first template of new category
    const template = this.allTemplates().find(t => t.category === category);
    if (template) {
      this.selectTemplate(template);
    } else {
      this.inputPrompt.set('');
      this.activeTemplateTitle.set('Custom Prompt');
    }
  }

  selectTemplate(template: Template) {
    this.inputPrompt.set(template.prompt);
    this.activeTemplateTitle.set(template.title);
  }

  onPromptChange(event: Event) {
    const value = (event.target as HTMLTextAreaElement).value;
    this.inputPrompt.set(value);
    // If text doesn't match any template exactly, set template title to Custom Prompt
    const matchesTemplate = this.allTemplates().some(t => t.prompt === value);
    if (!matchesTemplate) {
      this.activeTemplateTitle.set('Custom Prompt');
    }
  }

  generate() {
    if (!this.inputPrompt().trim()) return;

    this.isLoading.set(true);
    this.isComparing.set(false);
    this.comparisonRun.set(null);

    // If active execution exists and we have updated prompt, we might want to keep the same key to increment version,
    // otherwise generate a fresh group key (based on inputPrompt hash)
    let promptKey: string | undefined = undefined;
    if (this.activeExecution() && this.activeExecution()?.inputPrompt === this.inputPrompt()) {
      promptKey = this.activeExecution()?.promptKey;
    }

    const request: PromptRequest = {
      category: this.selectedCategory(),
      promptTemplate: this.activeTemplateTitle(),
      inputPrompt: this.inputPrompt(),
      promptKey: promptKey
    };

    this.promptService.generatePrompt(request).subscribe({
      next: (response) => {
        this.setActiveExecution(response);
        this.loadHistory();
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error generating AI output:', err);
        this.isLoading.set(false);
      }
    });
  }

  setActiveExecution(execution: PromptResponse | null) {
    this.activeExecution.set(execution);
    if (execution) {
      this.selectedCategory.set(execution.category);
      this.activeTemplateTitle.set(execution.promptTemplate);
      this.inputPrompt.set(execution.inputPrompt);
      this.reviewNotes.set(execution.reviewNotes || '');
      this.rating.set(execution.rating !== undefined ? execution.rating : null);
      
      // Load all versions for this promptKey
      this.promptService.getVersionsForComparison(execution.promptKey).subscribe({
        next: (versions) => {
          this.relatedVersions.set(versions);
          // Default compare candidate is the previous version (if it exists)
          const prevVersion = versions.find(v => v.version === execution.version - 1);
          if (prevVersion) {
            this.comparisonRun.set(prevVersion);
          } else {
            this.comparisonRun.set(null);
          }
        },
        error: (err) => console.error('Error fetching version history:', err)
      });
    } else {
      this.reviewNotes.set('');
      this.rating.set(null);
      this.relatedVersions.set([]);
      this.comparisonRun.set(null);
      this.isComparing.set(false);
    }
  }

  loadFromHistory(item: PromptResponse) {
    this.setActiveExecution(item);
  }

  setRating(value: number) {
    // Toggle rating
    if (this.rating() === value) {
      this.rating.set(null);
    } else {
      this.rating.set(value);
    }
    this.saveReview();
  }

  saveReview() {
    const active = this.activeExecution();
    if (!active) return;

    this.isSavingReview.set(true);
    this.promptService.updateReview(active.id, this.reviewNotes(), this.rating() || undefined).subscribe({
      next: (updated) => {
        this.activeExecution.set(updated);
        // Refresh history to update review notes in list
        this.loadHistory();
        this.isSavingReview.set(false);
      },
      error: (err) => {
        console.error('Error saving review notes:', err);
        this.isSavingReview.set(false);
      }
    });
  }

  toggleComparison() {
    this.isComparing.set(!this.isComparing());
  }

  selectComparisonVersion(versionNum: number) {
    const run = this.relatedVersions().find(v => v.version === versionNum);
    if (run) {
      this.comparisonRun.set(run);
    }
  }

  exportOutput() {
    const active = this.activeExecution();
    if (!active) return;

    const extension = this.getFileExtension(active.category, active.outputResponse);
    const filename = `${active.promptTemplate.replace(/\s+/g, '_')}_v${active.version}.${extension}`;
    const blob = new Blob([active.outputResponse], { type: 'text/plain;charset=utf-8' });
    
    // Create element and download
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    link.click();
    URL.revokeObjectURL(link.href);
  }

  getFileExtension(category: string, code: string): string {
    switch (category.toLowerCase()) {
      case 'code':
        if (code.includes('package ') || code.includes('import java.')) return 'java';
        if (code.includes('import ') && code.includes('from ')) return 'ts';
        if (code.includes('html')) return 'html';
        return 'java';
      case 'documentation':
        return 'md';
      case 'testing':
        return 'java';
      case 'design':
        return 'md';
      default:
        return 'txt';
    }
  }

  copyToClipboard() {
    const active = this.activeExecution();
    if (!active) return;

    navigator.clipboard.writeText(active.outputResponse).then(() => {
      alert('AI Output copied to clipboard!');
    });
  }

  resetEditor() {
    this.setActiveExecution(null);
    this.inputPrompt.set('');
    this.activeTemplateTitle.set('Custom Prompt');
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}
