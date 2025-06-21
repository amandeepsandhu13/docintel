export interface Table {
  rows: string[][];
}

export interface KeyValuePair {
  key: string;
  value: string;
}

export interface Chunk {
  index: number;
  text: string;
  embedding?: number[];
  sectionTitle?: string;
}

export interface ExtractedEntities {
  dates: string[];
  emails: string[];
  names: string[];
}

export interface SimpleAnalysisResult {
  content: string;
  keyValuePairs: KeyValuePair[];
  tables: Table[];
  unstructuredContent: string;
  chunks: Chunk[];
  extractedEntities: ExtractedEntities;
}
