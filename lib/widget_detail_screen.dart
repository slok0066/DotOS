import 'package:flutter/material.dart';
import 'core/theme.dart';
import 'core/components.dart';

class WidgetDetailScreen extends StatelessWidget {
  final String title;
  final String type;
  final Widget preview;
  final VoidCallback? onToggleTheme;

  const WidgetDetailScreen({
    super.key,
    required this.title,
    required this.type,
    required this.preview,
    this.onToggleTheme,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: NothingTheme.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text(
          title.toUpperCase(),
          style: NothingTheme.mono(fontSize: 14, color: NothingTheme.textPrimary),
        ),
        actions: [
          IconButton(
            onPressed: onToggleTheme,
            icon: Icon(
              Theme.of(context).brightness == Brightness.dark
                  ? Icons.light_mode_outlined
                  : Icons.dark_mode_outlined,
              color: NothingTheme.textPrimary,
            ),
          ),
        ],
      ),
      body: DotGridBackground(
        child: Padding(
          padding: const EdgeInsets.all(NothingTheme.spaceLG),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: NothingTheme.space2XL),
              Center(
                child: Hero(
                  tag: 'widget_$type',
                  child: SizedBox(
                    width: 300,
                    height: 150,
                    child: preview,
                  ),
                ),
              ),
              const SizedBox(height: NothingTheme.space4XL),
              Text(
                'PREVIEW',
                style: NothingTheme.mono(fontSize: 12, color: NothingTheme.textSecondary),
                textAlign: TextAlign.center,
              ),
              const Spacer(),
              Text(
                'Pin this widget to your home screen for quick access and real-time updates.',
                style: NothingTheme.mono(fontSize: 12, color: NothingTheme.textDisabled),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: NothingTheme.spaceLG),
              NothingButton(
                label: 'Add to Home',
                onTap: () {
                  WidgetService.addWidget(
                    type,
                    theme: Theme.of(context).brightness == Brightness.dark ? 'dark' : 'light',
                  );
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('PINNING ${title.toUpperCase()} WIDGET...'),
                      backgroundColor: NothingTheme.surfaceRaised,
                    ),
                  );
                },
              ),
              const SizedBox(height: NothingTheme.spaceXL),
            ],
          ),
        ),
      ),
    );
  }
}
