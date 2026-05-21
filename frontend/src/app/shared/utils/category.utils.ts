export class CategoryUtils {
  static getCategoryClass(category: string): string {
    const cat = (category || '').toLowerCase();
    if (cat.includes('deporte')) return 'cat-deportes';
    if (cat.includes('música') || cat.includes('musica')) return 'cat-musica';
    if (cat.includes('teatro')) return 'cat-teatro';
    if (cat.includes('comedia')) return 'cat-comedia';
    if (cat.includes('conferencia')) return 'cat-conferencia';
    if (cat.includes('arte')) return 'cat-arte';
    return 'cat-default';
  }

  static getCategoryIcon(category: string): string {
    const cat = (category || '').toLowerCase();
    if (cat.includes('deporte')) return '⚽';
    if (cat.includes('música') || cat.includes('musica')) return '🎵';
    if (cat.includes('teatro')) return '🎭';
    if (cat.includes('comedia')) return '😂';
    if (cat.includes('conferencia')) return '🎤';
    if (cat.includes('arte')) return '🎨';
    return '🎟️';
  }
}
